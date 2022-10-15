package dev.compactmods.machines.room;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.location.SimpleTeleporter;
import dev.compactmods.machines.room.client.RoomClientHelper;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import dev.compactmods.machines.room.server.RoomServerHelper;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RoomHelper {

    private static final Capability<IPlayerRoomMetadataProvider> CURRENT_ROOM_META = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Registry<RoomTemplate> getTemplates() {
        return DistExecutor.safeRunForDist(() -> RoomClientHelper::getTemplates, () -> RoomServerHelper::getTemplates);
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, Player player, LevelBlockPosition machinePos, IRoomRegistration room) throws MissingDimensionException {
        MinecraftServer serv = machineLevel.getServer();

        // Recursion check. Player tried to enter the room they're already in.
        if (player.level.dimension().equals(CompactDimension.LEVEL_KEY)) {
            final boolean recursion = player.getCapability(RoomCapabilities.ROOM_HISTORY).map(hist -> {
                if (player instanceof ServerPlayer sp && room.chunks().anyMatch(chunk -> sp.chunkPosition().equals(chunk))) {
                    AdvancementTriggers.RECURSIVE_ROOMS.trigger(sp);
                    return true;
                }

                return false;
            }).orElse(false);

            if (recursion) return;
        }

        try {
            final var entry = PreciseDimensionalPosition.fromPlayer(player);


            teleportPlayerIntoRoom(serv, player, room);

            // Mark the player as inside the machine, set external spawn, and yeet
            player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                hist.addHistory(new PlayerRoomHistoryItem(entry, machinePos));

                setCurrentRoom(serv, player, room);
            });
        } catch (MissingDimensionException | NonexistentRoomException e) {
            CompactMachines.LOGGER.fatal("Critical error; could not enter a freshly-created room instance.", e);
        }
    }

    public static void setCurrentRoom(MinecraftServer server, Player player, IRoomRegistration room) {
        // Mark current room, invalidates any listeners + debug screen
        final var roomProvider = CompactRoomProvider.instance(server);
        player.getCapability(CURRENT_ROOM_META).ifPresent(provider -> {
            provider.setCurrent(new PlayerRoomMetadataProvider.CurrentRoomData(room.code(), room.owner(roomProvider)));
        });
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, Player player, IRoomRegistration room) throws MissingDimensionException, NonexistentRoomException {
        teleportPlayerIntoRoom(serv, player, room, null);
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, Player player, IRoomRegistration room, @Nullable LevelBlockPosition from)
            throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = CompactDimension.forServer(serv);
        final var roomProvider = CompactRoomProvider.instance(compactDim);

        serv.submitAsync(() -> {

            if (player instanceof ServerPlayer servPlayer) {
                servPlayer.changeDimension(compactDim, SimpleTeleporter.to(room.spawnPosition(roomProvider), room.spawnRotation(roomProvider)));
            }
        });

        if(from != null) {
            // Mark the player as inside the machine, set external spawn
            player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                var entry = PreciseDimensionalPosition.fromPlayer(player);
                hist.addHistory(new PlayerRoomHistoryItem(entry, from));

            });
        }

        // Mark current room, invalidates any listeners + debug screen
        RoomHelper.setCurrentRoom(serv, player, room);
    }

    public static void teleportPlayerOutOfMachine(ServerLevel compactDim, @Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = compactDim.getServer();
        final var roomProvider = CompactRoomProvider.instance(compactDim);

        final LazyOptional<IRoomHistory> history = serverPlayer.getCapability(RoomCapabilities.ROOM_HISTORY);

        if (!history.isPresent()) {
            PlayerUtil.howDidYouGetThere(serverPlayer);
            return;
        }

        history.ifPresent(hist -> {
            if (hist.hasHistory()) {
                final IRoomHistoryItem prevArea = hist.pop();
                // Mark current room, invalidates any listeners + debug screen
                serverPlayer.getCapability(CURRENT_ROOM_META).ifPresent(provider -> {
                    roomProvider.findByChunk(prevArea.getEntryLocation().chunkPos()).ifPresent(roomMeta -> {
                        provider.setCurrent(new PlayerRoomMetadataProvider.CurrentRoomData(roomMeta.code(), roomMeta.owner(roomProvider)));
                    });
                });

                var spawnPoint = prevArea.getEntryLocation();
                final var enteredMachine = prevArea.getMachine().getBlockPosition();

                final var level = spawnPoint.level(serv);
                serverPlayer.changeDimension(level, SimpleTeleporter.lookingAt(spawnPoint.position(), enteredMachine));
            } else {
                PlayerUtil.howDidYouGetThere(serverPlayer);

                hist.clear();
                PlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            }
        });
    }
}
