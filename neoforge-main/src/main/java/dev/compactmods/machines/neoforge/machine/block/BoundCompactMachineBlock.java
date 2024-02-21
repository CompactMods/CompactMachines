package dev.compactmods.machines.neoforge.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.Messages;
import dev.compactmods.machines.api.machine.MachineCreator;
import dev.compactmods.machines.api.machine.item.IBoundCompactMachineItem;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.EnumMachinePlayersBreakHandling;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.room.RoomHelper;
import dev.compactmods.machines.neoforge.room.ui.MachineRoomMenu;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoundCompactMachineBlock extends Block implements EntityBlock {
    public BoundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity be) {
            return MachineCreator.boundToRoom(be.connectedRoom(), be.getColor());
        }

        LoggingUtil.modLog().warn("Warning: tried to pick block on a machine that does not have an associated block entity.");
        return MachineCreator.unbound();
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        int baseSpeedForge = CommonHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
        float normalHardness = player.getDigSpeed(state, pos) / baseSpeedForge;

        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity bound) {
            boolean hasPlayers = bound.hasPlayersInside();

            // If there are players inside, check config for break handling
            if (hasPlayers) {
                EnumMachinePlayersBreakHandling hand = ServerConfig.MACHINE_PLAYER_BREAK_HANDLING.get();
                switch (hand) {
                    case UNBREAKABLE:
                        return 0;

                    case OWNER:
                        Optional<UUID> ownerUUID = bound.getOwnerUUID();
                        return ownerUUID
                                .map(uuid -> player.getUUID() == uuid ? normalHardness : 0)
                                .orElse(normalHardness);

                    case ANYONE:
                        return normalHardness;
                }
            }

            // No players inside - let anyone break it
            return normalHardness;
        } else {
            return normalHardness;
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {
            // force client redraw
            if (stack.getItem() instanceof IBoundCompactMachineItem bound) {
                if (!level.isClientSide) {
                    bound.getRoom(stack).ifPresent(roomCode -> {
                        if(placer instanceof ServerPlayer sp && RoomHelper.entityInsideRoom(sp, roomCode)) {
                            // TODO: Ouroboros advancement
                        }

                        tile.setConnectedRoom(roomCode);
                    });
                } else {
                    final int color = bound.getMachineColor(stack);
                    tile.setColor(color);
                }
//                    PacketDistributor.TRACKING_CHUNK.with(level.getChunkAt(pos))
//                            .send(ClientboundBlockEntityDataPacket.create(tile));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoundCompactMachineBlockEntity(pos, state);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.is(PSDTags.ITEM)
                && player instanceof ServerPlayer sp
                && level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {

            // Try to teleport player into room
            RoomHelper.teleportPlayerIntoMachine(level, sp, tile.getLevelPosition(), tile.connectedRoom());
            return InteractionResult.SUCCESS;
        }

        // All other items, open preview screen
        if (!level.isClientSide) {
            level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(machine -> {
                final var roomCode = machine.connectedRoom();
                if (player instanceof ServerPlayer sp) {

                    sp.openMenu(MachineRoomMenu.makeProvider(sp.server, roomCode, machine.getLevelPosition()), (buf) -> {
                        buf.writeBlockPos(pos);
                        buf.writeJsonWithCodec(GlobalPos.CODEC, machine.getLevelPosition());
                        buf.writeUtf(roomCode);

                        // FIXME Renamable rooms
//            roomName.ifPresentOrElse(name -> {
//                buf.writeBoolean(true);
//                buf.writeUtf(name);
//            }, () -> {
                        buf.writeBoolean(false);
                        buf.writeUtf("");
//            });
                    });
                }
            });
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    public static InteractionResult tryApplyNametag(Level level, BlockPos pos, Player player) {
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.getItem() instanceof NameTagItem && mainItem.hasCustomHoverName()) {
            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {
                boolean isOwner = tile.owner.equals(player.getUUID());
                boolean isOp = player.hasPermissions(Commands.LEVEL_MODERATORS);

                if (!isOp || !isOwner) {
                    final var ownerProfile = tile.getOwnerUUID().flatMap(id -> PlayerUtil.getProfileByUUID(level, id));
                    ownerProfile.ifPresent(owner -> {
                        player.displayClientMessage(TranslationUtil.message(Messages.CANNOT_RENAME_NOT_OWNER,
                                owner.getName()), true);
                    });
                }

                final var newName = mainItem.getHoverName().getString(120);
                // FIXME Renamable rooms Rooms.updateName(level.getServer(), tile.connectedRoom(), newName);
            }
        }
        return null;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
        // TODO: Remove machine from room graph
    }
}
