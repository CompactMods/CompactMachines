package dev.compactmods.machines.forge.room;

import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.dimension.SimpleTeleporter;
import dev.compactmods.machines.forge.network.CompactMachinesNet;
import dev.compactmods.machines.forge.network.SyncRoomMetadataPacket;
import dev.compactmods.machines.forge.room.capability.RoomCapabilities;
import dev.compactmods.machines.forge.util.ForgePlayerUtil;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.room.client.RoomClientHelper;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RoomHelper {

    public static final Capability<IPlayerRoomMetadataProvider> CURRENT_ROOM_META = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Registry<RoomTemplate> getTemplates() {
        return DistExecutor.safeRunForDist(() -> RoomClientHelper::getTemplates, () -> ForgeRoomServerHelper::getTemplates);
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) throws MissingDimensionException {
        MinecraftServer serv = machineLevel.getServer();

        CompactRoomProvider.instance(serv)
                .forRoom(roomCode)
                .ifPresent(roomInfo -> {
                    // Recursion check. Player tried to enter the room they're already in.
                    if (player.level.dimension().equals(CompactDimension.LEVEL_KEY)) {
                        final boolean recursion = player.getCapability(RoomCapabilities.ROOM_HISTORY).map(hist -> {
                            if (roomInfo.chunks().anyMatch(chunk -> player.chunkPosition().equals(chunk))) {
                                AdvancementTriggers.RECURSIVE_ROOMS.trigger(player);
                                return true;
                            }

                            return false;
                        }).orElse(false);

                        if (recursion) return;
                    }

                    try {
                        final var entry = PreciseDimensionalPosition.fromPlayer(player);

                        teleportPlayerIntoRoom(serv, player, roomInfo);

                        // Mark the player as inside the machine, set external spawn, and yeet
                        player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                            hist.addHistory(new PlayerRoomHistoryItem(entry, machinePos));
                        });
                    } catch (MissingDimensionException | NonexistentRoomException e) {
                        CompactMachines.LOGGER.fatal("Critical error; could not enter a freshly-created room instance.", e);
                    }
                });
    }

    public static void setCurrentRoom(MinecraftServer server, ServerPlayer player, IRoomRegistration room) {
        // Mark current room, invalidates any listeners + debug screen
        final var roomProvider = CompactRoomProvider.instance(server);
        final var roomOwner = room.owner(roomProvider);
        player.getCapability(CURRENT_ROOM_META).ifPresent(provider -> {
            provider.setCurrent(new PlayerRoomMetadataProvider.CurrentRoomData(room.code(), roomOwner));
        });

        final var sync = new SyncRoomMetadataPacket(room.code(), roomOwner);
        CompactMachinesNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), sync);
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, IRoomRegistration room) throws MissingDimensionException, NonexistentRoomException {
        teleportPlayerIntoRoom(serv, player, room, null);
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, IRoomRegistration room, @Nullable GlobalPos from)
            throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = CompactDimension.forServer(serv);
        final var roomProvider = CompactRoomProvider.instance(compactDim);

        serv.submitAsync(() -> {
            player.changeDimension(compactDim, SimpleTeleporter.to(room.spawnPosition(roomProvider), room.spawnRotation(roomProvider)));
        });

        if (from != null) {
            // Mark the player as inside the machine, set external spawn
            player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                var entry = PreciseDimensionalPosition.fromPlayer(player);
                hist.addHistory(new PlayerRoomHistoryItem(entry, from));
            });
        }

        // Mark current room, invalidates any listeners + debug screen
        RoomHelper.setCurrentRoom(serv, player, room);
    }

    public static void teleportPlayerOutOfRoom(@Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = serverPlayer.getServer();
        if (!serverPlayer.level.dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        serverPlayer.getCapability(RoomCapabilities.ROOM_HISTORY)
                .resolve()
                .ifPresentOrElse(hist -> {
                    if (hist.hasHistory()) {
                        final var roomProvider = CompactRoomProvider.instance(serv);
                        final IRoomHistoryItem prevArea = hist.pop();
                        // Mark current room, invalidates any listeners + debug screen
                        serverPlayer.getCapability(CURRENT_ROOM_META).ifPresent(provider -> {
                            // Check entry dimension - if it isn't a machine room, clear room info
                            if (!prevArea.getEntryLocation().dimension().equals(CompactDimension.LEVEL_KEY))
                                provider.clearCurrent();
                            else {
                                roomProvider.findByChunk(prevArea.getEntryLocation().chunkPos()).ifPresent(roomMeta -> {
                                    provider.setCurrent(new PlayerRoomMetadataProvider.CurrentRoomData(roomMeta.code(), roomMeta.owner(roomProvider)));
                                });
                            }
                        });

                        var spawnPoint = prevArea.getEntryLocation();
                        final var enteredMachine = prevArea.getMachine().pos();

                        final var level = spawnPoint.level(serv);
                        serverPlayer.changeDimension(level, SimpleTeleporter.lookingAt(spawnPoint.position(), enteredMachine));
                    } else {
                        PlayerUtil.howDidYouGetThere(serverPlayer);

                        hist.clear();
                        ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                    }
                }, () -> {
                    PlayerUtil.howDidYouGetThere(serverPlayer);
                    ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                });
    }
}
