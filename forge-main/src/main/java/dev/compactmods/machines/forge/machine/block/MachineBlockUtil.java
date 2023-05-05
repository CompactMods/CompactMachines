package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.forge.config.ServerConfig;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.forge.room.Rooms;
import dev.compactmods.machines.forge.room.ui.MachineRoomMenu;
import dev.compactmods.machines.forge.wall.Walls;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelMachineFilters;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("removal")
public class MachineBlockUtil {

    static void cleanupTunnelsPostMachineRemove(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl) {
            final var serv = sl.getServer();
            final var compactDim = serv.getLevel(CompactDimension.LEVEL_KEY);

            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity entity) {
                entity.connectedRoom().ifPresent(roomCode -> {
                    final var dimGraph = DimensionMachineGraph.forDimension(sl);
                    dimGraph.unregisterMachine(pos);

                    if (compactDim == null)
                        return;

                    final var tunnels = TunnelConnectionGraph.forRoom(compactDim, roomCode);
                    tunnels.positions(TunnelMachineFilters.all(entity.getLevelPosition()))
                            .forEach(pos1 -> {
                                tunnels.unregister(pos1);
                                compactDim.setBlock(pos1, Walls.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
                            });
                });
            }
        }
    }

    @Nonnull
    static InteractionResult tryRoomTeleport(Level level, BlockPos pos, ServerPlayer player, MinecraftServer server) {
        // Try teleport to compact machine dimension
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {
            tile.connectedRoom().ifPresentOrElse(roomCode -> {
                try {
                    RoomHelper.teleportPlayerIntoMachine(level, player, tile.getLevelPosition(), roomCode);
                } catch (MissingDimensionException e) {
                    e.printStackTrace();
                }
            }, () -> {

                // AdvancementTriggers.getTriggerForMachineClaim(size).trigger(sp);
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Gets destroy progress, without any lookups on owner, internal players, etcetera.
     *
     * @param state
     * @param player
     * @param worldIn
     * @param pos
     * @return
     */
    public static float destroyProgressUnchecked(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        int baseSpeedForge = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
        return player.getDigSpeed(state, pos) / (float) baseSpeedForge;
    }

    public static float destroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        float normalHardness = destroyProgressUnchecked(state, player, worldIn, pos);

        BoundCompactMachineBlockEntity tile = (BoundCompactMachineBlockEntity) worldIn.getBlockEntity(pos);
        if (tile == null)
            return normalHardness;

        boolean hasPlayers = tile.hasPlayersInside();

        // If there are players inside, check config for break handling
        if (hasPlayers) {
            EnumMachinePlayersBreakHandling hand = ServerConfig.MACHINE_PLAYER_BREAK_HANDLING.get();
            switch (hand) {
                case UNBREAKABLE:
                    return 0;

                case OWNER:
                    Optional<UUID> ownerUUID = tile.getOwnerUUID();
                    return ownerUUID
                            .map(uuid -> player.getUUID() == uuid ? normalHardness : 0)
                            .orElse(normalHardness);

                case ANYONE:
                    return normalHardness;
            }
        }

        // No players inside - let anyone break it
        return normalHardness;
    }

    public static void roomPreviewScreen(BlockPos pos, ServerPlayer player, MinecraftServer server, BoundCompactMachineBlockEntity machine) {
        machine.connectedRoom().ifPresent(roomCode -> {
            try {
                final var roomName = Rooms.getRoomName(server, roomCode);
                NetworkHooks.openScreen(player, MachineRoomMenu.makeProvider(server, roomCode, machine.getLevelPosition()), (buf) -> {
                    buf.writeBlockPos(pos);
                    buf.writeWithCodec(GlobalPos.CODEC, machine.getLevelPosition());
                    buf.writeUtf(roomCode);
                    roomName.ifPresentOrElse(name -> {
                        buf.writeBoolean(true);
                        buf.writeUtf(name);
                    }, () -> {
                        buf.writeBoolean(false);
                        buf.writeUtf("");
                    });
                });
            } catch (NonexistentRoomException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    public static InteractionResult tryApplyNametag(Level level, BlockPos pos, Player player) {
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.getItem() instanceof NameTagItem && mainItem.hasCustomHoverName()) {
            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {
                final var ownerProfile = tile.getOwnerUUID().flatMap(id -> PlayerUtil.getProfileByUUID(level, id));
                boolean isOwner = ownerProfile.map(p -> p.getId().equals(player.getUUID())).orElse(false);
                boolean isOp = player.hasPermissions(Commands.LEVEL_MODERATORS);

                if (ownerProfile.isEmpty()) {
                    return InteractionResult.FAIL;
                }

                if (!isOp || !isOwner)
                    return InteractionResult.FAIL;
                else {
                    ownerProfile.ifPresent(owner -> {
                        player.displayClientMessage(TranslationUtil.message(Messages.CANNOT_RENAME_NOT_OWNER,
                                owner.getName()), true);
                    });
                }

                tile.connectedRoom().ifPresent(roomCode -> {
                    try {
                        final var newName = mainItem.getHoverName().getString(120);
                        Rooms.updateName(level.getServer(), roomCode, newName);
                    } catch (NonexistentRoomException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return null;
    }
}
