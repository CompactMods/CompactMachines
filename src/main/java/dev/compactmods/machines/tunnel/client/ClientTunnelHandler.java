package dev.compactmods.machines.tunnel.client;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientTunnelHandler {
    public static void setTunnel(BlockPos position, TunnelDefinition type) {
        var level = Minecraft.getInstance().level;
        if(level == null) return;

        if(level.getBlockEntity(position) instanceof TunnelWallEntity tun) {
            try {
                tun.setTunnelType(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
