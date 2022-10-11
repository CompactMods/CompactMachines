package dev.compactmods.machines.core;

import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

public class UIRegistration {

    public static final RegistryObject<MenuType<MachineRoomMenu>> MACHINE_MENU = Registries.CONTAINERS.register("machine", () -> IForgeMenuType.create(
            ((windowId, inv, data) -> {
                data.readBlockPos();
                final var mach = data.readWithCodec(LevelBlockPosition.CODEC);
                final var room = data.readUtf();
                final boolean hasName = data.readBoolean();
                final var roomName = hasName ? data.readUtf() : "Room Preview";

                return new MachineRoomMenu(windowId, room, mach, roomName);
            })
    ));

    public static void prepare() {

    }
}
