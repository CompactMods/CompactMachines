package dev.compactmods.machines.room.upgrade.example;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeType;
import dev.compactmods.machines.api.room.upgrade.events.RoomUpgradeEvent;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeRemovedEventListener;
import dev.compactmods.machines.room.upgrade.RoomUpgrades;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class ChunkLoaderUpgrade implements RoomUpgrade {

    public static final MapCodec<ChunkLoaderUpgrade> CODEC = MapCodec.unit(ChunkLoaderUpgrade::new);

    @Override
    public RoomUpgradeType<?> getType() {
        return RoomUpgrades.CHUNK_LOADER.get();
    }

    @Override
    public Stream<RoomUpgradeEvent> gatherEvents() {
        UpgradeAppliedEventListener onApplied = this::applied;
        UpgradeRemovedEventListener onRemoved = this::removed;
        return Stream.of(onApplied, onRemoved);
    }

    private void applied(RoomUpgradeInstance roomUpgradeInstance) {

    }

    private void removed(RoomUpgradeInstance roomUpgradeInstance) {

    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(Component.literal("Chunk Loader (Not Yet Functional)").withColor(CommonColors.LIGHT_GRAY));
    }
}
