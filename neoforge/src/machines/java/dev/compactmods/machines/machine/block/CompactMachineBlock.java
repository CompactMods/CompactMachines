package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.generation.RoomGenerationException;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.IBoundCompactMachineBlockEntity;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.MachineRoomEntry;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.compactmods.machines.machine.network.MachineColorSyncPacket;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
import dev.compactmods.machines.shrinking.api.ShrinkingDeviceConfiguration;
import dev.compactmods.machines.shrinking.history.UsedShrinkingDeviceOnMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class CompactMachineBlock extends Block implements EntityBlock {
    public CompactMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Two adjacent compact machines should read as one continuous glass surface:
     * the cullface'd panes on the touching faces drop out, leaving only the frame
     * bars at the seam. Same pattern vanilla {@code HalfTransparentBlock} uses.
     */
    @Override
    protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
        return neighborState.is(this) || super.skipRendering(state, neighborState, direction);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        try {
            if (level.getBlockEntity(pos) instanceof CompactMachineBlockEntity be) {
                if (includeData && be.connectedRoom().isPresent()) {
                    final var room = be.connectedRoom().get();
                    final var stack = Machines.Items.boundToRoom(room);

                    be.getCustomName().ifPresent(cn -> {
                        stack.set(DataComponents.CUSTOM_NAME, cn);
                    });

                    return stack;
                } else {
                    return Machines.Items.MACHINE.toStack();
                }
            }

            return Machines.Items.MACHINE.toStack();
        } catch (Exception ex) {
            CompactMachinesCore.modLog().warn("Warning: tried to pick block on a bound machine that does not have a room bound.", ex);
            return Machines.Items.MACHINE.toStack();
        }
    }

    @NotNull
    protected static InteractionResult tryDyingMachine(ServerLevel level, @NotNull BlockPos pos, Player player, ItemStack mainItem) {
        final var dye = mainItem.get(DataComponents.DYE);
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICompactMachineBlockEntity cmbe) {
            final var newColor = MachineColor.fromDyeColor(dye);
            cmbe.setMachineColor(newColor);

            PacketDistributor.sendToPlayersTrackingChunk(
                    level, ChunkPos.containing(pos), new MachineColorSyncPacket(GlobalPos.of(level.dimension(), pos), newColor));

            if (!player.isCreative())
                mainItem.shrink(1);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompactMachineBlockEntity(pos, state);
    }

    @NonNull
    @Override
    @ParametersAreNonnullByDefault
    protected InteractionResult useItemOn(ItemStack mainItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel sl))
            return InteractionResult.SUCCESS;

        final var server = sl.getServer();
        final var roomRegistry = server.getCapability(RoomCapabilities.REGISTRY);

        if (mainItem.isEmpty())
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (mainItem.has(DataComponents.DYE)) {
            return tryDyingMachine(sl, pos, player, mainItem);
        }

        final var tile = level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get())
                .orElse(null);

        if (tile == null)
            return InteractionResult.FAIL;

        // FIXME: Core overwrite bug (should swap cores, not overwrite)
        if (mainItem.has(Rooms.DataComponents.BOUND_ROOM_CODE) || mainItem.has(Rooms.DataComponents.ROOM_TEMPLATE_ID)) {
            boolean yay = tile.setCore(serverPlayer, mainItem);
            return yay ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        // Try to teleport player into room
        if (mainItem.has(Shrinking.DataComponents.SHRINKING_CONFIG)) {
            var config = mainItem.getOrDefault(Shrinking.DataComponents.SHRINKING_CONFIG, ShrinkingDeviceConfiguration.DEFAULT_CONFIG);

            final var currentCore = tile.coreHandler().getResource(0);
            if(currentCore.isEmpty())
                return InteractionResult.FAIL;


            if(currentCore.has(Rooms.DataComponents.BOUND_ROOM_CODE)) {

                // var shrinking = level.getCapability(Shrinking.SHRINK_HANDLER);
                // shrinking.enter(player, mainItem);

                final var room = roomRegistry
                        .get(currentCore.get(Rooms.DataComponents.BOUND_ROOM_CODE))
                        .orElse(null);

                if (room != null) {
                    MachineRoomEntry.tryEnter(serverPlayer, room, tile, mainItem);
                }
            }

            if(currentCore.has(Rooms.DataComponents.ROOM_TEMPLATE_ID)) {
                final var templateId = currentCore.get(Rooms.DataComponents.ROOM_TEMPLATE_ID);
                final var template = RoomTemplateHelper.getTemplate(level, templateId);

                try {
                    final var generator = server.getCapability(RoomCapabilities.GENERATOR);
                    if(generator == null)
                        return InteractionResult.FAIL;

                    final var details = generator.createNew()
                            .template(template)
                            .owner(player.getUUID())
                            .build();

                    generator.generate(details).ifPresent(result -> {
                        final var room = result.newCode();

                        // Audit
                        final var log = CompactMachinesCore.modLog();
                        log.info("Generated room: {}", room);
                        log.debug("Template used: {}", templateId);
                        log.debug("Player: {}", player.nameAndId().name());

                        // Swap the core template for the room details
                        final var newCore = currentCore.toStack(1);
                        newCore.remove(Rooms.DataComponents.ROOM_TEMPLATE_ID);
                        newCore.set(Rooms.DataComponents.BOUND_ROOM_CODE, room);
                        tile.setCore(player, newCore);

                        // Enter the room
                        final var instance = roomRegistry.get(room).orElseThrow();
                        MachineRoomEntry.tryEnter(serverPlayer, instance, tile, mainItem);
                    });
                } catch (RoomGenerationException e) {
                    throw new RuntimeException(e);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // All other items, open preview screen
        if (level.isClientSide() || player instanceof FakePlayer) {
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            InteractionResult.Success successServer = tryRemoveCore(level, pos, player);
            if (successServer != null) return successServer;
        }

        if (player instanceof ServerPlayer sp) {
            final var machinePos = GlobalPos.of(level.dimension(), pos);
            sp.setData(Machines.Attachments.OPEN_MACHINE_POS, machinePos);

            sp.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                    return new MachineUIMenu(containerId, p, machinePos);
                }

                @Override
                public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
                    MachineUIMenu.writeClientSideData(menu, buffer);
                }
            });
//                                                PacketDistributor.sendToPlayer(sp,
//                                                        new OpenMachinePreviewScreenPacket(GlobalPos.of(level.dimension(), pos), roomCode)
//                                                );
        }

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult.@org.jspecify.annotations.Nullable Success tryRemoveCore(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof IBoundCompactMachineBlockEntity bound) {
            final var core = bound.popCore(player);
            player.setItemSlot(EquipmentSlot.MAINHAND, core);
            return InteractionResult.SUCCESS_SERVER;
        }
        return null;
    }
}
