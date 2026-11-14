package dev.compactmods.machines.upgrades.example;

import dev.compactmods.machines.upgrades.RoomUpgrades;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;
import dev.compactmods.machines.upgrades.api.event.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.upgrades.api.event.lifecycle.UpgradeRemovedEventListener;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Keeps a room's chunks force-loaded with full simulation, as if a player were standing inside.
 *
 * <p>This component does not tick. It reacts to lifecycle events: when the upgrade is enabled on a
 * room it force-loads the room's inner chunks (ticking tickets, so entities and block ticks run even
 * with nobody nearby); when disabled it releases them. The tickets themselves are persisted by
 * NeoForge and managed through {@link ChunkLoaderTickets}.
 */
public class ChunkLoaderUpgradeComponent implements RoomUpgradeComponent {

    @Override
    public RoomUpgradeComponentType<?> getType() {
        return RoomUpgrades.CHUNK_LOADER.get();
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        consumer.accept(Component.literal("Chunk Loader").withColor(CommonColors.LIGHT_GRAY));
    }

    @Override
    public Stream<RoomUpgradeComponentEvent> gatherEvents() {
        return Stream.of(
                (UpgradeAppliedEventListener) room -> ChunkLoaderTickets.setForced(room, true),
                (UpgradeRemovedEventListener) room -> ChunkLoaderTickets.setForced(room, false)
        );
    }
}
