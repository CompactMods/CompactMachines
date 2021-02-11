package com.robotgryphon.compactmachines.api.tunnels.redstone;

import com.robotgryphon.compactmachines.api.tunnels.ITunnelConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * A redstone reader reads a redstone value from outside the machine (from a given side).
 */
public interface IRedstoneReaderTunnel extends IRedstoneTunnel {

    int getStrongPower(ITunnelConnectionInfo connectionInfo);

    int getWeakPower(ITunnelConnectionInfo connectionInfo);

    /**
     * Called by the tunnel wall blocks to check if a redstone signal can be pulled
     * from the outside. This should almost always return true, but if you need custom
     * logic then you can implement it here.
     *
     * @param connectionInfo Connection information for the tunnel instance.
     * @return
     */
    default boolean canConnectRedstone(ITunnelConnectionInfo connectionInfo) {
        return true;
    }

    void onPowerChanged(ITunnelConnectionInfo connectionInfo, int latestPower);
}
