package org.dave.CompactMachines.api;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.world.IBlockAccess;

/**
 * @author soniex2
 */
public interface IBlockProxy {
    final class BlockLocation {
        private final IBlockAccess world;
        private final int x, y, z;
        private final ForgeDirection facing;

        /**
         * Constructs a new BlockLocation.
         *
         * @param world  The world.
         * @param x      The X position.
         * @param y      The Y position.
         * @param z      The Z position.
         * @param facing The direction to access the block from.
         */
        public BlockLocation(IBlockAccess world, int x, int y, int z, ForgeDirection facing) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing;
        }

        public IBlockAccess getWorld() {
            return this.world;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        /**
         * Returns the direction to access this block from.
         */
        public ForgeDirection getFacing() {
            return this.facing;
        }
    }

    BlockLocation[] getConnectedBlocks(ForgeDirection facing);
}
