package dev.compactmods.machines.client.creative;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

import static dev.compactmods.machines.CMRegistries.TABS;

public interface CreativeTabs {

    Identifier MAIN_RL = CompactMachinesCore.identifier("main");

    static void prepare() {
        TABS.register(MAIN_RL.getPath(), () -> CreativeModeTab.builder()
            .icon(() -> Machines.Items.MACHINE.toStack(1))
            .title(Component.translatableWithFallback("itemGroup.compactmachines.main", "Compact Machines"))
            .displayItems(CreativeTabs::fillItems)
            .build());
    }

    static void fillItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {
        output.accept(Rooms.Items.BREAKABLE_WALL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        output.accept(Machines.Items.MACHINE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Shrinking.Items.PERSONAL_SHRINKING_DEVICE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Shrinking.Items.SHRINKING_MODULE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Shrinking.Items.ENLARGING_MODULE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Tunnels.Items.TUNNEL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        // TODO: Items with a room mapping (paper room cores?)
//        final var lookup = params.holders().lookupOrThrow(RoomTemplate.REGISTRY_KEY);
//        final var machines = lookup.listElements()
//            .map(Machines.Items::forNewRoom)
//            .toList();
//
//        output.acceptAll(machines, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
