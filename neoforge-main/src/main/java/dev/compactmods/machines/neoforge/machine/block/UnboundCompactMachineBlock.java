package dev.compactmods.machines.neoforge.machine.block;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.machine.MachineCreator;
import dev.compactmods.machines.api.machine.item.IUnboundCompactMachineItem;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.neoforge.room.RoomHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnboundCompactMachineBlock extends Block implements EntityBlock {
    public UnboundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof UnboundCompactMachineEntity be) {
            final var temp = be.template().orElseThrow();
            return UnboundCompactMachineItem.forTemplate(be.templateId(), temp);
        }

        return MachineCreator.unbound();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new UnboundCompactMachineEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.getBlockEntity(pos, Machines.UNBOUND_MACHINE_ENTITY.get()).ifPresent(tile -> {
            if(stack.getItem() instanceof IUnboundCompactMachineItem unbound) {
                final var template = unbound.getTemplateId(stack);
                tile.setTemplate(template);
            }
        });
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        MinecraftServer server = level.getServer();
        ItemStack mainItem = player.getMainHandItem();

        if (mainItem.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            level.getBlockEntity(pos, Machines.UNBOUND_MACHINE_ENTITY.get()).ifPresent(unboundEntity -> {
                RoomTemplate template = unboundEntity.template().orElse(RoomTemplate.INVALID_TEMPLATE);
                if (!template.equals(RoomTemplate.INVALID_TEMPLATE)) {
                    try {
                        // Generate a new machine room
                        final var newRoom = RoomApi.newRoom(server, template, sp.getUUID());

                        // Change into a bound machine block
                        level.setBlock(pos, Machines.MACHINE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);

                        // Set up binding and enter
                        level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(ent -> {
                            ent.setConnectedRoom(newRoom.code());

                            try {
                                RoomHelper.teleportPlayerIntoRoom(server, sp, newRoom, RoomEntryPoint.playerEnteringMachine(player));
                            } catch (MissingDimensionException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    } catch (MissingDimensionException e) {
                        LoggingUtil.modLog().error("Error occurred while generating new room and machine info for first player entry.", e);
                    }
                } else {
                    LoggingUtil.modLog().fatal("Tried to create and enter an invalidly-registered room. Something went very wrong!");
                }
            });
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
