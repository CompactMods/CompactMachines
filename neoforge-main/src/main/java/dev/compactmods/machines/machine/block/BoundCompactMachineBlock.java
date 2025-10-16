package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.machine.OpenMachinePreviewScreenPacket;
import dev.compactmods.machines.room.RoomBlocks;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;

public class BoundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public BoundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        try {
            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity be) {
                final var stack = Machines.Items.boundToRoom(be.connectedRoom(), be.getMachineColor());

                be.getCustomName().ifPresent(cn -> {
                    stack.set(DataComponents.CUSTOM_NAME, cn);
                });

                return stack;
            }

            return Machines.Items.unbound();
        } catch (Exception ex) {
            LoggingUtil.modLog().warn("Warning: tried to pick block on a bound machine that does not have a room bound.", ex);
            return Machines.Items.unbound();
        }
    }

//    @Override
//    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
//        int baseSpeedForge = EventHooks.doPlayerHarvestCheck(player, state, level, pos) ? 30 : 100;
//        final var normalHardness = player.getDigSpeed(state, pos) / baseSpeedForge;
//
//        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity bound) {
//            boolean hasPlayers = bound.hasPlayersInside();
//
//            // If there are players inside, check config for break handling
//            if (hasPlayers) {
//                EnumMachinePlayersBreakHandling hand = ServerConfig.MACHINE_PLAYER_BREAK_HANDLING.get();
//                switch (hand) {
//                    case UNBREAKABLE:
//                        return 0;
//
//                    case OWNER:
//                        Optional<UUID> ownerUUID = bound.getOwnerUUID();
//                        return ownerUUID
//                                .map(uuid -> player.getUUID() == uuid ? normalHardness : 0)
//                                .orElse(normalHardness);
//
//                    case ANYONE:
//                        return normalHardness;
//                }
//            }
//
//            // No players inside - let anyone break it
//            return normalHardness;
//        } else {
//            return normalHardness;
//        }
//    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoundCompactMachineBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack mainItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (mainItem.getItem() instanceof DyeItem dye && !level.isClientSide() && level instanceof ServerLevel sl) {
            return tryDyingMachine(sl, pos, player, dye, mainItem);
        }

        if (mainItem.has(Shrinking.DataComponents.SHRINKING_CONFIG)
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity tile) {

            // Try to teleport player into room
            RoomHelper.teleportPlayerIntoMachine(level, serverPlayer, tile.getLevelPosition(), tile.connectedRoom()).thenAccept(result -> {
                if (result.successful()) {
                    var config = PersonalShrinkingDevice.config(mainItem);
                    PersonalShrinkingDevice.handleSuccessfulAtomicShift(mainItem, serverPlayer, config);
                }
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // All other items, open preview screen
        if (!level.isClientSide() && !(player instanceof FakePlayer)) {
            level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get()).ifPresent(machine -> {
                final var roomCode = machine.connectedRoom();
                CompactMachines.room(roomCode).ifPresent(inst -> {
                    if (player instanceof ServerPlayer sp) {
                        sp.setData(CMDataAttachments.OPEN_MACHINE_POS, machine.getLevelPosition());

                        try {
                            final var roomBlocks = RoomBlocks.getInternalBlocks(level.getServer(), inst).get();

                            PacketDistributor.sendToPlayer(sp,
                                    new OpenMachinePreviewScreenPacket(GlobalPos.of(level.dimension(), pos), roomCode, roomBlocks)
                            );
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
