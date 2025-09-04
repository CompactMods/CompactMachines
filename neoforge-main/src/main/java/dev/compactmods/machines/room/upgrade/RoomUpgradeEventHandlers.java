package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;
import dev.compactmods.machines.api.room.upgrade.event.level.LevelLoadedUpgradeEventListener;
import dev.compactmods.machines.api.room.upgrade.event.level.LevelUnloadedUpgradeEventListener;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeTickedEventListener;
import dev.compactmods.machines.api.room.upgrade.inventory.RoomUpgradeInventory;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventHandler;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RoomUpgradeEventHandlers {

    public static void collectUpgradeEvents() {
        Set<Class<? extends Event>> allEvents = new HashSet<>();

        RoomUpgrades.ROOM_UPGRADE_DEFINITIONS.getRegistry()
                .get()
                .forEach(upgradeType -> {
                    var inst = upgradeType.constructor().get();
                    if (inst instanceof NeoForgeEventListener listener) {
                        var eventTypes = listener.gatherNeoEvents()
                                .map(NeoForgeEventHandler::eventType)
                                .distinct()
                                .toList();

                        allEvents.addAll(eventTypes);
                    }
                });

        allEvents.forEach(RoomUpgrades::eventProcessor);
    }

    private static void doRoomUpgradeLoop(BiConsumer<RoomInstance, RoomUpgradeInventory> forEach) {
        final var rooms = CompactMachines.roomRegistrar();
        if (rooms != null) {
            final var registeredRooms = rooms.allRooms()
                    .collect(Collectors.toUnmodifiableSet());

            for (var room : registeredRooms) {
                room.getCapability(RoomCapabilities.ROOM_DATA_ATTACHMENTS)
                        .getExistingData(CMDataAttachments.UPGRADE_ITEMS)
                        .ifPresent(i -> forEach.accept(room, i));
            }
        }
    }

    public static void cleanupDeadUpgrades(final ServerStartedEvent serverStartedEvent) {
        if (!CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(serverStartedEvent.getServer().getWorldData().enabledFeatures()))
            return;

        CompactMachines.roomRegistrar()
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

    private static <Evt extends RoomUpgradeComponentEvent> void handleBasicEvent(RoomInstance room, @NotNull RoomUpgradeInventory appliedUpgrades, Class<Evt> eventType) {
        final var upgradeInstances = appliedUpgrades.items()
                .map(stack -> stack.get(CMDataComponents.UPGRADE_INSTANCE_ID))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        final var upgradeAccessor = room.getCapability(RoomCapabilities.UPGRADES);
        if (upgradeAccessor != null) {
            for (final var upgradeId : upgradeInstances) {
                final var instance = upgradeAccessor.getOrCreateInstance(upgradeId);
                if (instance.upgradeItem().isEmpty()) {
                    upgradeAccessor.remove(instance.upgradeID());
                    instance.upgradeItem().remove(CMDataComponents.UPGRADE_INSTANCE_ID);
                    return;
                }

                final var upgrades = instance.upgradeItem().get(CMDataComponents.UPGRADE_LIST_COMPONENT);
                if (upgrades != null) {
                    upgrades.components()
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

        if (stack.has(CMDataComponents.UPGRADE_INSTANCE_ID)) {
            var id = stack.get(CMDataComponents.UPGRADE_INSTANCE_ID);
            tooltips.accept(Component.literal("ID: " + id).withColor(CommonColors.GRAY));
        }

        stack.addToTooltip(CMDataComponents.UPGRADE_LIST_COMPONENT, ctx, tooltips, flags);
    }
}
