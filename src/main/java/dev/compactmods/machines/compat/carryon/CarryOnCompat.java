package dev.compactmods.machines.compat.carryon;

import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.room.RoomSize;
import net.minecraftforge.fml.InterModComms;

public class CarryOnCompat {

    private static final String MOD_ID = "carryon";

    public static void sendIMC() {
        // Denies the machine blocks from being picked up by CarryOn users. Prevents dupes.
        for(var size : RoomSize.values()) {
            final var regName = CompactMachineBlock.getBySize(size).getRegistryName();
            InterModComms.sendTo(MOD_ID, "blacklistBlock", () -> regName.toString());
        }
    }
}
