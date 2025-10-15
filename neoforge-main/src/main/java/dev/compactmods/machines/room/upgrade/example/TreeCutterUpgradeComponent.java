package dev.compactmods.machines.room.upgrade.example;

import com.google.common.base.Predicates;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponent;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponentType;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeTickedEventListener;
import dev.compactmods.machines.room.upgrade.RoomUpgrades;
import dev.compactmods.spatial.aabb.AABBHelper;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CommonColors;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeCutterUpgradeComponent implements RoomUpgradeComponent {

    public static final MapCodec<TreeCutterUpgradeComponent> CODEC = MapCodec.unit(TreeCutterUpgradeComponent::new);

    public enum SuccessfulActionBehavior {
        DoNothing,
        DamageItem,
        DrainEnergy
    }

    public static final Supplier<AttachmentType<Data>> TREECUTTER_DATA = CMDataAttachments.ATTACHMENT_TYPES
            .register("treecutter", key -> AttachmentType.builder(() -> new Data())
                    .serialize(Data.CODEC)
                    .build());

    public static void prepare() {
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        final var c = Component.literal("Tree Cutter")
                .withColor(CommonColors.LIGHT_GRAY);

        consumer.accept(c);
    }

    @Override
    public Stream<RoomUpgradeComponentEvent> gatherEvents() {
        final UpgradeTickedEventListener ticker = TreeCutterUpgradeComponent::onTick;
        return Stream.of(ticker);
    }

    @Override
    public RoomUpgradeComponentType<TreeCutterUpgradeComponent> getType() {
        return RoomUpgrades.TREECUTTER.get();
    }

    public static void onTick(RoomUpgradeInstance instance) {
        final var data = instance.getData(TREECUTTER_DATA);

        if (data.cooldown > 0) {
            data.cooldown--;
            return;
        }

        final var room = instance.roomInstance();
        final var level = room.level();
        final var everythingLoaded = room.boundaries()
                .innerChunkPositions()
                .allMatch(cp -> level.shouldTickBlocksAt(cp.toLong()));

        if (!everythingLoaded) {
            data.cooldown = 200;
            return;
        }

        final var innerBounds = room.boundaries().innerBounds();

        final var upgradeItem = instance.upgradeItem();
        var energyHandler = ItemAccess.forStack(upgradeItem).getCapability(Capabilities.Energy.ITEM);

        SuccessfulActionBehavior behavior = SuccessfulActionBehavior.DoNothing;
        int maxAllowed = 0;
        if (upgradeItem.isDamageableItem()) {
            behavior = SuccessfulActionBehavior.DamageItem;
            var durabilityLeft = upgradeItem.getMaxDamage() - upgradeItem.getDamageValue();
            maxAllowed = Math.clamp(durabilityLeft, 0, 5);
        }

        if (energyHandler != null) {
            behavior = SuccessfulActionBehavior.DrainEnergy;
            maxAllowed = Math.clamp(energyHandler.getAmountAsLong() / 10, 0, 10);
        }

        final var treeBlocks = BlockPos.betweenClosedStream(innerBounds)
                .map(pos -> {
                    final var state = level.getBlockState(pos);
                    return Pair.of(pos.immutable(), state);
                })
                .filter(pair -> {
                    BlockState state = pair.right();
                    if (state.is(BlockTags.LOGS)) return true;
                    if (state.is(BlockTags.LEAVES)) {
                        if (state.hasProperty(LeavesBlock.PERSISTENT)) return !state.getValue(LeavesBlock.PERSISTENT);
                        return true;
                    }

                    return false;
                })
                .limit(maxAllowed)
                .collect(Collectors.toUnmodifiableSet());

        final var numLogs = treeBlocks.size();

        if (!treeBlocks.isEmpty()) {
            final var bounds = room.boundaries().innerBounds();
            final var inventories = getInventories(level, bounds).toList();

            // If we have no valid inventories, do nothing
            if (inventories.isEmpty())
                return;

            for (Pair<BlockPos, BlockState> pos : treeBlocks) {
                breakSingleBlock(pos, level, upgradeItem, inventories, energyHandler, behavior);
            }
        }

        data.cooldown = 1;
    }

    private static void breakSingleBlock(Pair<BlockPos, BlockState> pos, ServerLevel level, ItemStack upgradeItem, List<LocatedInventory> inventories, EnergyHandler energyHandler, SuccessfulActionBehavior actionBehavior) {
        final var blockEntity = level.getBlockEntity(pos.left());

        final var drops = Block.getDrops(pos.right(), level, pos.left(), blockEntity, null, upgradeItem);
        ResourceHandler<ItemResource> memory = new ItemStacksResourceHandler(NonNullList.copyOf(drops));

        try (final var blockTx = Transaction.openRoot()) {
            switch (actionBehavior) {
                case DoNothing:
                    break;

                case DamageItem:
                    // FIXME - Revert item damage if drops did not fit
                    upgradeItem.hurtAndBreak(1, level, null, (item) -> {
                        upgradeItem.shrink(1);
                    });
                    break;

                case DrainEnergy:
                    int pulled = energyHandler.extract(10, blockTx);

                    // Revert transaction, abort
                    if (pulled < 10) return;
                    break;
            }

            if (!drops.isEmpty()) {

                for (final var cornerInv : inventories) {
                    int moved = ResourceHandlerUtil.move(memory, cornerInv.inventory, Predicates.alwaysTrue(), Integer.MAX_VALUE, blockTx);

                    // Nothing left to move or nothing moved - exit
                    if (moved == 0)
                        break;
                }

                if (ResourceHandlerUtil.isEmpty(memory)) {
                    level.destroyBlock(pos.left(), false);
                    blockTx.commit();
                }
            }
        }
    }

    private record LocatedInventory(BlockPos pos, ResourceHandler<ItemResource> inventory) {
    }

    private static Stream<LocatedInventory> getInventories(ServerLevel level, AABB bounds) {
        return AABBHelper.allCorners(bounds)
                .map(BlockPos::immutable)
                .flatMap(pos -> Stream.of(
                                level.getCapability(Capabilities.Item.BLOCK, pos, null),
                                level.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP)
                        )
                        .filter(Objects::nonNull)
                        .map(handler -> new LocatedInventory(pos, handler)));
    }

    public static class Data {
        public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown").forGetter(d -> d.cooldown)
        ).apply(i, Data::new));

        public int cooldown;

        public Data() {
            this.cooldown = 0;
        }

        Data(int cooldown) {
            this.cooldown = cooldown;
        }
    }
}
