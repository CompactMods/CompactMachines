package dev.compactmods.machines.upgrades.example;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;
import dev.compactmods.machines.upgrades.api.event.lifecycle.TickingRoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.event.lifecycle.UpgradeRemovedEventListener;
import dev.compactmods.machines.upgrades.RoomUpgrades;
import dev.compactmods.machines.upgrades.canvas.CanvasUpgradeComponent;
import dev.compactmods.machines.upgrades.canvas.RoomCanvasContext;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.function.Consumer;

/**
 * Cuts down trees inside the room. It takes one axe at a time out of the tools storage and wears it
 * one point per block until it breaks (the axe in use lives in the room's {@link TreeCutterState}, and
 * is given back if the upgrade is disabled); everything dropped goes to the drops storage — both set on the room's upgrade canvas through the
 * {@link TreeCutterUpgradeNode tree cutter node}, which this component provides, and kept in its own
 * {@link TreeCutterState}. Does nothing until both are set, or while switched off on the node.
 *
 * <p>Replants: cutting a tree's bottom log leaves a stump (remembered in the state), and the first
 * sapling from the drops storage that can grow there is planted on it — straight away, or once the
 * leaves have dropped one.
 */
public class TreeCutterUpgradeComponent implements CanvasUpgradeComponent, TickingRoomUpgradeComponent {

    /** Most blocks cut per tick. */
    private static final int MAX_BLOCKS_PER_TICK = 5;

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        final var c = Component.literal("Tree Cutter")
                .withColor(CommonColors.LIGHT_GRAY);

        consumer.accept(c);
    }

    @Override
    public RoomUpgradeComponentType<TreeCutterUpgradeComponent> getType() {
        return RoomUpgrades.TREECUTTER.get();
    }

    @Override
    public Stream<RoomUpgradeComponentEvent> gatherEvents() {
        return Stream.of((UpgradeRemovedEventListener) TreeCutterUpgradeComponent::returnAxe);
    }

    @Override
    public void contribute(RoomCanvasContext canvas) {
        final var room = canvas.room();
        // Placed nodes start switched on
        final var node = canvas.provide(TreeCutterUpgradeNode.KEY, Map.of(TreeCutterUpgradeNode.ENABLED.name(), true));
        canvas.watch(node, TreeCutterUpgradeNode.ENABLED, enabled -> TreeCutterState.update(room, state -> state.withEnabled(enabled.orElse(false))));
        canvas.watch(node, TreeCutterUpgradeNode.TOOLS, tools -> TreeCutterState.update(room, state -> state.withTools(tools)));
        canvas.watch(node, TreeCutterUpgradeNode.DROPS, drops -> TreeCutterState.update(room, state -> state.withDrops(drops)));
    }

    public void tick(RoomInstance instance) {
        final var level = instance.level();
        final var everythingLoaded = instance.boundaries()
                .innerChunkPositions()
                .allMatch(cp -> level.shouldTickBlocksAt(cp.pack()));

        if (!everythingLoaded)
            return;

        final var state = TreeCutterState.get(instance);
        if (!state.enabled())
            return;

        final var tools = state.toolsHandler(level.getServer()).orElse(null);
        final var drops = state.dropsHandler(level.getServer()).orElse(null);
        if (tools == null || drops == null)
            return;

        final var treeBlocks = BlockPos.betweenClosedStream(instance.boundaries().innerBounds())
                .map(pos -> Pair.of(pos.immutable(), level.getBlockState(pos)))
                .filter(pair -> isTreeBlock(pair.right()))
                .limit(MAX_BLOCKS_PER_TICK)
                .toList();

        ItemStack axe = state.axe();
        final List<BlockPos> stumps = new ArrayList<>(state.stumps());
        for (Pair<BlockPos, BlockState> block : treeBlocks) {
            if (axe.isEmpty())
                axe = takeAxe(tools);
            if (axe.isEmpty() || !cut(level, block.left(), block.right(), axe, drops))
                break; // out of axes or room for drops; the rest would fail the same way

            if (isStump(level, block.left(), block.right()) && !stumps.contains(block.left()))
                stumps.add(block.left());
        }

        // After cutting, so saplings dropped this tick can go straight back in
        stumps.removeIf(stump -> replant(level, stump, drops));
        while (stumps.size() > TreeCutterState.MAX_STUMPS)
            stumps.removeFirst();

        final ItemStack heldAxe = axe;
        TreeCutterState.update(instance, current -> current.withAxe(heldAxe).withStumps(stumps));
    }

    /** A log standing on ground that grows plants: where its tree grew, so where to replant. */
    private static boolean isStump(ServerLevel level, BlockPos pos, BlockState state) {
        return state.is(BlockTags.LOGS) && level.getBlockState(pos.below()).is(BlockTags.SUPPORTS_VEGETATION);
    }

    /**
     * Plants a sapling from {@code drops} at {@code stump} — the first one that can grow there.
     *
     * @return {@code true} once the stump is dealt with: planted, or no longer plantable (built over, or
     * its ground is gone). {@code false} keeps it waiting for a sapling.
     */
    private static boolean replant(ServerLevel level, BlockPos stump, ResourceHandler<ItemResource> drops) {
        if (!level.getBlockState(stump).isAir() || !level.getBlockState(stump.below()).is(BlockTags.SUPPORTS_VEGETATION))
            return true;

        for (int slot = 0; slot < drops.size(); slot++) {
            final ItemResource resource = drops.getResource(slot);
            if (resource.isEmpty() || !(resource.getItem() instanceof BlockItem item) || !resource.toStack().is(ItemTags.SAPLINGS))
                continue;

            final BlockState sapling = item.getBlock().defaultBlockState();
            if (!sapling.canSurvive(level, stump))
                continue;

            try (final var tx = Transaction.openRoot()) {
                if (drops.extract(slot, resource, 1, tx) == 1 && level.setBlock(stump, sapling, Block.UPDATE_ALL)) {
                    tx.commit();
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isTreeBlock(BlockState state) {
        if (state.is(BlockTags.LOGS)) return true;
        if (state.is(BlockTags.LEAVES)) {
            if (state.hasProperty(LeavesBlock.PERSISTENT)) return !state.getValue(LeavesBlock.PERSISTENT);
            return true;
        }

        return false;
    }

    /**
     * Cuts one block with {@code axe}, all or nothing: the block only breaks if every drop fits in
     * {@code drops}. The axe then wears one point — down to empty once it breaks.
     *
     * @return {@code false} if it couldn't, so the caller stops for this tick
     */
    private static boolean cut(ServerLevel level, BlockPos pos, BlockState state, ItemStack axe,
                               ResourceHandler<ItemResource> drops) {
        final List<ItemStack> dropped = Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, axe);

        try (final var tx = Transaction.openRoot()) {
            for (ItemStack stack : dropped) {
                if (!stack.isEmpty() && drops.insert(ItemResource.of(stack), stack.getCount(), tx) != stack.getCount())
                    return false;
            }

            if (!level.destroyBlock(pos, false))
                return false;

            tx.commit();
        }

        axe.hurtAndBreak(1, level, (LivingEntity) null, broken -> {});
        return true;
    }

    /** Takes one axe out of {@code tools}. {@return it}, or empty if there are none */
    private static ItemStack takeAxe(ResourceHandler<ItemResource> tools) {
        for (int slot = 0; slot < tools.size(); slot++) {
            final ItemResource resource = tools.getResource(slot);
            if (resource.isEmpty() || !resource.toStack().is(ItemTags.AXES))
                continue;

            try (final var tx = Transaction.openRoot()) {
                if (tools.extract(slot, resource, 1, tx) == 1) {
                    tx.commit();
                    return resource.toStack(1);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gives back the axe in use when the upgrade is disabled: to the tools storage, else the drops
     * storage, else dropped in the middle of the room.
     */
    private static void returnAxe(RoomInstance room) {
        final var state = TreeCutterState.get(room);
        final ItemStack axe = state.axe();
        if (axe.isEmpty())
            return;

        final var level = room.level();
        final var server = level.getServer();
        final ItemResource resource = ItemResource.of(axe);
        final boolean stored = Stream.of(state.toolsHandler(server), state.dropsHandler(server))
                .flatMap(Optional::stream)
                .anyMatch(handler -> {
                    try (final var tx = Transaction.openRoot()) {
                        if (handler.insert(resource, axe.getCount(), tx) != axe.getCount())
                            return false;
                        tx.commit();
                        return true;
                    }
                });

        if (!stored)
            Block.popResource(level, BlockPos.containing(room.boundaries().innerBounds().getCenter()), axe);

        TreeCutterState.update(room, current -> current.withAxe(ItemStack.EMPTY));
    }
}
