package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import net.minecraft.core.GlobalPos;
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
                final var mach = data.readWithCodec(GlobalPos.CODEC);
                final var room = data.readChunkPos();
                final boolean hasName = data.readBoolean();
                final var roomName = hasName ? data.readUtf() : "Room Preview";

                return new MachineRoomMenu(windowId, room, mach, roomName);
            })
    ));

    public static void init(IEventBus bus) {
        CONTAINERS.register(bus);
    }
}
