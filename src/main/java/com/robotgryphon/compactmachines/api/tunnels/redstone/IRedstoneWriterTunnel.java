package com.robotgryphon.compactmachines.api.tunnels.redstone;

import com.robotgryphon.compactmachines.api.tunnels.ITunnelConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * A redstone writer sends a redstone value from inside the machine (redstone connected to a tunnel).
 */
public interface IRedstoneWriterTunnel extends IRedstoneTunnel {

    int getStrongPower(ITunnelConnectionInfo connectionInfo);

    int getWeakPower(ITunnelConnectionInfo connectionInfo);
}
