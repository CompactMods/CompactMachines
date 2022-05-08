package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class UIRegistration {
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CompactMachines.MOD_ID);

    public static final RegistryObject<MenuType<MachineRoomMenu>> MACHINE_MENU = CONTAINERS.register("machine", () -> IForgeMenuType.create(
            ((windowId, inv, data) -> {
                data.readBlockPos();
                final int mach = data.readInt();
                final var room = data.readChunkPos();

                return new MachineRoomMenu(windowId, mach, room);
            })
    ));

    public static void init(IEventBus bus) {
        CONTAINERS.register(bus);
    }
}
