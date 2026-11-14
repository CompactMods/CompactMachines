package dev.compactmods.machines.machine.network.client;

import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.core.machine.MachineColor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.Block;

/// Client-side handling for machine network packets. Kept inside :machines so the
/// module owns its own packet + handler pair (mirrors
/// dev.compactmods.machines.shrinking.network.client.ClientShrinkingPacketHandler).
public class ClientMachinePacketHandler {
    public static void setMachineColor(GlobalPos position, MachineColor newColor) {
        var mc = Minecraft.getInstance();
        assert mc.level != null;
        if (mc.level.dimension() == position.dimension()) {
            var state = mc.level.getBlockState(position.pos());
            var blockEntity = mc.level.getBlockEntity(position.pos());

            if (blockEntity instanceof ICompactMachineBlockEntity cmbe) {
                cmbe.setMachineColor(newColor);
                mc.level.sendBlockUpdated(position.pos(), state, state, Block.UPDATE_ALL_IMMEDIATE);
            }
        }
    }
}
