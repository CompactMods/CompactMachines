package dev.compactmods.machines.neoforge.client.creative;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.machine.MachineCreator;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.compactmods.machines.neoforge.Registries.TABS;

public interface CreativeTabs {

    ResourceLocation MAIN_RL = new ResourceLocation(Constants.MOD_ID, "main");
    ResourceLocation LINKED_MACHINES_RL = new ResourceLocation(Constants.MOD_ID, "linked_machines");

    DeferredHolder<CreativeModeTab, CreativeModeTab> NEW_MACHINES = TABS.register(MAIN_RL.getPath(), () -> CreativeModeTab.builder()
            .icon(MachineCreator::unbound)
            .title(Component.translatableWithFallback("itemGroup.compactmachines.main", "Compact Machines"))
            .displayItems(CreativeTabs::fillItems)
            .build());

    DeferredHolder<CreativeModeTab, CreativeModeTab> EXISTING_MACHINES = TABS.register(LINKED_MACHINES_RL.getPath(), () -> CreativeModeTab.builder()
            .icon(() -> {
                final var ub = MachineCreator.unboundColored(CompactMachines.BRAND_MACHINE_COLOR);
                return ub;
            })
            .title(Component.translatableWithFallback("itemGroup.compactmachines.linked_machines", "Linked Machines"))
            .withTabsBefore(MAIN_RL)
            .withSearchBar()
            .build());

    static void fillItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Rooms.ITEM_BREAKABLE_WALL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Shrinking.SHRINKING_MODULE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Shrinking.ENLARGING_MODULE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        // output.accept(Shrinking.RESIZING_MODULE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        final var lookup = params.holders().lookupOrThrow(RoomTemplate.REGISTRY_KEY);
        final var machines = lookup.listElements()
                .map(k -> UnboundCompactMachineItem.forTemplate(k.key().location(), k.value()))
                .toList();

        output.acceptAll(machines, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    static void prepare() {}
}
