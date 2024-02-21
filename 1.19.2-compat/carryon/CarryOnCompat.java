package dev.compactmods.machines.neoforge.compat.carryon;

import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.api.room.RoomSize;
import net.neoforged.fml.InterModComms;
import net.neoforged.registries.ForgeRegistries;

public class CarryOnCompat {

    private static final String MOD_ID = "carryon";

    @SuppressWarnings("removal")
    public static void sendIMC() {
        // Denies the machine blocks from being picked up by CarryOn users.
        // Prevents duplication of data on block movement.
        final var blockReg = ForgeRegistries.BLOCKS;
        for(var size : RoomSize.values()) {
            final var regName = blockReg.getKey(LegacySizedCompactMachineBlock.getBySize(size));
            if(regName != null)
                InterModComms.sendTo(MOD_ID, "blacklistBlock", regName::toString);
        }

        final var machineBlockNew = blockReg.getKey(Machines.MACHINE_BLOCK.get());
        if(machineBlockNew != null)
            InterModComms.sendTo(MOD_ID, "blacklistBlock", machineBlockNew::toString);
    }
}
