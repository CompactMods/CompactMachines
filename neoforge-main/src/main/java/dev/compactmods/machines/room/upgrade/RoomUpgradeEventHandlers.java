package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.events.RoomUpgradeEvent;
import dev.compactmods.machines.api.room.upgrade.events.level.LevelLoadedUpgradeEventListener;
import dev.compactmods.machines.api.room.upgrade.events.level.LevelUnloadedUpgradeEventListener;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeTickedEventListener;
import dev.compactmods.machines.api.room.upgrade.inventory.RoomUpgradeInventory;
import dev.compactmods.machines.feature.CMFeatureFlags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RoomUpgradeEventHandlers {

    private static void doRoomUpgradeLoop(BiConsumer<RoomInstance, RoomUpgradeInventory> forEach) {
        final var rooms = CompactMachines.roomApi();
        if (rooms != null) {
            final var registeredRooms = rooms.registrar()
                    .allRooms()
                    .collect(Collectors.toUnmodifiableSet());

            for (var room : registeredRooms) {
                CompactMachines.existingRoomData(room.code())
                        .flatMap(rd -> rd.getExistingData(CMDataAttachments.UPGRADE_ITEMS))
                        .ifPresent(i -> forEach.accept(room, i));
            }
        }
    }

    public static void onServerTick(final ServerTickEvent.Post postTick) {
        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(postTick.getServer().getWorldData().enabledFeatures()))
            return;

        CompactMachines.roomApi()
                .registrar()
                .allRooms()
                .forEach(RoomUpgradeHelper::cleanDeadUpgrades);
    }

    public static void onLevelLoad(final LevelEvent.Load loaded) {
        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(loaded.getLevel().enabledFeatures()))
            return;

        if (loaded.getLevel() instanceof ServerLevel serverLevel && CompactDimension.isLevelCompact(serverLevel)) {
            doRoomUpgradeLoop((room, inv) -> handleBasicEvent(room, inv, LevelLoadedUpgradeEventListener.class));
        }
    }

    public static void onLevelUnload(final LevelEvent.Unload loaded) {
        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(loaded.getLevel().enabledFeatures()))
            return;

        if (loaded.getLevel() instanceof ServerLevel serverLevel && CompactDimension.isLevelCompact(serverLevel)) {
            doRoomUpgradeLoop((room, inv) -> handleBasicEvent(room, inv, LevelUnloadedUpgradeEventListener.class));
        }
    }

    public static void onLevelTick(LevelTickEvent.Post postTick) {
        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(postTick.getLevel().enabledFeatures()))
            return;

        if (postTick.getLevel() instanceof ServerLevel serverLevel && CompactDimension.isLevelCompact(serverLevel)) {
            doRoomUpgradeLoop((room, inv) -> handleBasicEvent(room, inv, UpgradeTickedEventListener.class));
        }
    }

    private static <Evt extends RoomUpgradeEvent> void handleBasicEvent(RoomInstance room, @NotNull RoomUpgradeInventory appliedUpgrades, Class<Evt> eventType) {
        final var upgradeInstances = appliedUpgrades.items()
                .map(stack -> stack.get(CMDataComponents.UPGRADE_INSTANCE_ID))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        final var upgradeAccessor = CompactMachines.upgradeAccessor();
        if(upgradeAccessor != null) {
            for (final var upgradeId : upgradeInstances) {
                final var instance = upgradeAccessor.getOrCreateInstance(room, upgradeId);
                if (instance.upgradeItem().isEmpty()) {
                    upgradeAccessor.removeInstance(instance.upgradeID());
                    instance.upgradeItem().remove(CMDataComponents.UPGRADE_INSTANCE_ID);
                    return;
                }

                final var upgrades = instance.upgradeItem().get(CMDataComponents.UPGRADE_LIST_COMPONENT);
                if (upgrades != null) {
                    upgrades.upgrades()
                            .stream()
                            .flatMap(ru -> ru.gatherEvents().filter(eventType::isInstance))
                            .map(eventType::cast)
                            .forEach(loadHandler -> loadHandler.handle(instance));
                }
            }
        }
    }

    public static void onTooltips(ItemTooltipEvent evt) {
        Item.TooltipContext ctx = evt.getContext();
        Consumer<Component> tooltips = evt.getToolTip()::add;
        TooltipFlag flags = evt.getFlags();

        ItemStack stack = evt.getItemStack();

        stack.addToTooltip(CMDataComponents.UPGRADE_LIST_COMPONENT, ctx, tooltips, flags);
    }
}
