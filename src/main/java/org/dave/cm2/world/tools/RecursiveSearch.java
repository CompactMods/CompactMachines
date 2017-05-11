package org.dave.cm2.world.tools;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.cm2.utility.Logz;

import java.util.ArrayList;
import java.util.HashMap;

public class RecursiveSearch {
    private HashMap<BlockPos, Boolean> visited = new HashMap<>();
    private ArrayList<BlockPos> result = new ArrayList<>();

    private World world;
    private Block type;
    private int limit = 2048;
    private int lowestY = -1;
    private boolean matchBlock = true;
    private BlockPos startPos;

    public RecursiveSearch(World world, BlockPos pos, Block type, int lowestY, boolean matchBlock) {
        this.world = world;
        this.type = type;
        this.lowestY = lowestY;
        this.matchBlock = matchBlock;
        this.startPos = pos;

        getConnectedBlocks(pos);
    }

    public void getConnectedBlocks(BlockPos pos) {
        if (visited.getOrDefault(pos, false)) {
            return;
        }
        if (lowestY != -1 && pos.getY() < lowestY) {
            return;
        }
        visited.put(pos, true);
        limit--;
        if (limit <= 0) {
            return;
        }

        IBlockState state = world.getBlockState(pos);
        if (matchBlock && state.getBlock() != type) {
            return;
        }
        if (!matchBlock && state.getBlock() == type) {
            return;
        }

        if(!state.getBlock().isAir(state, world, pos)) {
            result.add(pos);
        }

        for (EnumFacing direction : EnumFacing.values()) {
            getConnectedBlocks(pos.offset(direction));
        }
    }

    public ArrayList<BlockPos> getResult() {
        return result;
    }
}
