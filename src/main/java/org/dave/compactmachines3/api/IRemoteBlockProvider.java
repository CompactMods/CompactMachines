package org.dave.compactmachines3.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;

/**
 * This interface is implemented on Machines and Tunnels.
 * The data provided here is only available on the server. Calling these
 * methods on the client will probably crash the game. Don't do it.
 *
 * If you need to know stuff about the other block (e.g. whether to render
 * a "connection-plug" or just continue with some cables) you have to
 * network that data yourself - but you probably are already syncing the
 * connection type of a blocks face from the server to the client anyway.
 */
public interface IRemoteBlockProvider {
    /**
     * Returns the position of the connected block. null if no block is connected.
     * Side is only relevant for machine blocks and is ignored for tunnel blocks.
     *
     * @param side
     * @return
     */
    @Nullable
    BlockPos getConnectedBlockPosition(EnumFacing side);

    /**
     * Returns the dimension id of the connected block.
     * Side is only relevant for machine blocks and is ignored for tunnel blocks.
     * On a machine block this always returns the id of the machine dimension (-144 by default)
     * This returns 0 if no block is connected, i.e. make sure to check the result of
     * getConnectedBlockPosition before using this information.
     *
     * @param side
     * @return
     */
    int getConnectedDimensionId(EnumFacing side);

    /**
     * Returns the WorldServer for the dimension of the connected block.
     * This is just a convenience wrapper around the getConnectedDimensionId method to
     * directly grab a reference to the world, which allows you to call getTileEntity
     * etc.
     *
     * Again and as the return value implies: Don't call this on a client.
     *
     * @param side
     * @return
     */
    default WorldServer getConnectedDimension(EnumFacing side) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(this.getConnectedDimensionId(side));
    }
}
