package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.machine.entity.UnboundCompactMachineEntity;
import dev.compactmods.machines.forge.machine.item.MachineItemUtil;
import dev.compactmods.machines.forge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.forge.wall.Walls;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.util.CompactStructureGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class UnboundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public UnboundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new UnboundCompactMachineEntity(pos, state);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return MachineBlockUtil.destroyProgressUnchecked(state, player, level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.getBlockEntity(pos, Machines.UNBOUND_MACHINE_ENTITY.get()).ifPresent(tile -> {
            if(!level.isClientSide) {
                final var template = MachineItemUtil.getTemplateId(stack);
                tile.setTemplate(template);
            }
        });
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab tab, @NotNull NonNullList<ItemStack> tabItems) {
        var reg = RoomHelper.getTemplates();
        // TODO - fix ordering
        tabItems.addAll(reg.entrySet()
                .stream()
                .map((template) -> UnboundCompactMachineItem.forTemplate(template.getKey().location(), template.getValue()))
                .collect(Collectors.toSet()));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        MinecraftServer server = level.getServer();
        ItemStack mainItem = player.getMainHandItem();

        if (mainItem.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            level.getBlockEntity(pos, Machines.UNBOUND_MACHINE_ENTITY.get()).ifPresent(unboundEntity -> {
                RoomTemplate template = unboundEntity.template().orElse(RoomTemplate.INVALID_TEMPLATE);
                if(!template.equals(RoomTemplate.INVALID_TEMPLATE))
                {
                    try {
                        final var compactDim = CompactDimension.forServer(server);
                        if (template.equals(RoomTemplate.INVALID_TEMPLATE)) {
                            CompactMachines.LOGGER.fatal("Tried to create and enter an invalidly-registered room. Something went very wrong!");
                            return;
                        }

                        final var roomInfo = CompactRoomProvider.instance(compactDim);
                        final var newRoom = roomInfo.registerNew(builder -> builder
                                .setColor(template.color())
                                .setDimensions(template.dimensions())
                                .setOwner(player.getUUID()));

                        // Generate a new machine room
                        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
                        CompactStructureGenerator.generateRoom(compactDim, template.dimensions(), newRoom.center(), unbreakableWall);

                        // If template specified, prefill new room
                        if (!template.prefillTemplate().equals(RoomTemplate.NO_TEMPLATE)) {
                            CompactStructureGenerator.fillWithTemplate(compactDim, template.prefillTemplate(), template.dimensions(), newRoom.center());
                        }

                        level.setBlock(pos, Machines.MACHINE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);

                        level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(ent -> {
                            ent.setConnectedRoom(newRoom.code());
                            try {
                                RoomHelper.teleportPlayerIntoRoom(server, sp, newRoom, ent.getLevelPosition());
                            } catch (MissingDimensionException | NonexistentRoomException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    } catch (MissingDimensionException e) {
                        CompactMachines.LOGGER.error("Error occurred while generating new room and machine info for first player entry.", e);
                    }
                }
            });
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
