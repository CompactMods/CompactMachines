package dev.compactmods.machines.tunnel.client;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

import java.util.List;

public class ClientTunnelHandler {
    public static void setTunnel(BlockPos position, TunnelDefinition type) {
        var level = Minecraft.getInstance().level;
        if(level == null) return;

        Minecraft.getInstance().tell(() -> {
            if (level.getBlockEntity(position) instanceof TunnelWallEntity tun) {
                try {
                    tun.setTunnelType(type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void updateMachineTunnels(BlockPos machine, List<GlobalPos> tunnels) {
        final var mc = Minecraft.getInstance();
        if(mc.level.getBlockEntity(machine) instanceof CompactMachineBlockEntity cmTile) {
            cmTile.updateTunnelList(tunnels);
        }
    }
}
