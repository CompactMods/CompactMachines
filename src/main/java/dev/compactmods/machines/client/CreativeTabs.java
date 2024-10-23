package dev.compactmods.machines.client;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.TunnelItem;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.tunnel.definitions.ItemTunnel;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

public class CreativeTabs {
    public static ResourceLocation MAIN_RL = new ResourceLocation(Constants.MOD_ID, "main");

    public static final RegistryObject<CreativeModeTab> TAB = Registries.TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(() -> Machines.MACHINE_BLOCK_ITEM_NORMAL.get().getDefaultInstance())
                    .displayItems(CreativeTabs::fillItems)
                    .title(Component.translatableWithFallback("itemGroup.compactmachines.main", "Compact Machines"))
                    .build()
    );

    private static void fillItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output) {

        /*final var lookup = params.holders().lookupOrThrow(RoomTemplate.REGISTRY_KEY);
        final var machines = lookup.listElements()
                .map(k -> Machines.Items.forNewRoom(k.key().location(), k.value()))
                .toList();*/
        final var machines = Arrays
                .stream(new Item[]{
                        Machines.MACHINE_BLOCK_ITEM_TINY.get(),
                        Machines.MACHINE_BLOCK_ITEM_SMALL.get(),
                        Machines.MACHINE_BLOCK_ITEM_NORMAL.get(),
                        Machines.MACHINE_BLOCK_ITEM_LARGE.get(),
                        Machines.MACHINE_BLOCK_ITEM_GIANT.get(),
                        Machines.MACHINE_BLOCK_ITEM_MAXIMUM.get()
                })
                .map(Item::getDefaultInstance)
                .toList();
        output.acceptAll(machines, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        output.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Walls.ITEM_BREAKABLE_WALL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        output.accept(Walls.ITEM_SOLID_WALL.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

        var tunnels = Tunnels.TUNNEL_DEF_REGISTRY
                .get()
                .getValues()
                .stream()
                .filter(d -> d != Tunnels.UNKNOWN.get())
                .map(TunnelItem::createStack)
                .toList();
        output.acceptAll(tunnels);
    }

    public static void prepare() {

    }
}
