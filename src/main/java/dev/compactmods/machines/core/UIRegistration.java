package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.ui.CompactMachineRoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class UIRegistration {
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CompactMachines.MOD_ID);

    public static final RegistryObject<MenuType<CompactMachineRoomMenu>> MACHINE_MENU = CONTAINERS.register("machine", () -> IForgeMenuType.create(
            ((windowId, inv, data) -> {
                final var pos = data.readBlockPos();
                final var lev = inv.player.getCommandSenderWorld();
                return new CompactMachineRoomMenu(windowId, lev, pos, inv.player);
            })
    ));

    public static void init(IEventBus bus) {
        CONTAINERS.register(bus);
    }
}
