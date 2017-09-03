package org.dave.compactmachines3.miniaturization;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.dave.compactmachines3.utility.Logz;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiblockRecipe {
    private String name;

    private String[][][] map;
    private String[][][] map90;
    private String[][][] map180;
    private String[][][] map270;
    private List<BlockPos> mapAsBlockPos;
    private HashMap<String, IBlockState> reference;
    private HashMap<String, Integer> referenceCount;

    private BlockPos minPos;
    private BlockPos maxPos;

    private ItemStack targetStack;

    private Item catalyst;
    private int catalystMeta;
    private int count;

    private int ticks;

    private boolean symmetrical;

    public MultiblockRecipe(String name, ItemStack targetStack, Item catalyst, int catalystMeta, boolean symmetrical, int ticks) {
        this.name = name;
        this.reference = new HashMap<>();
        this.referenceCount = new HashMap<>();
        this.targetStack = targetStack;
        this.catalyst = catalyst;
        this.catalystMeta = catalystMeta;
        this.symmetrical = symmetrical;
        this.ticks = ticks;
    }

    public void addBlockReference(String ref, IBlockState state) {
        this.reference.put(ref, state);
    }

    public int getTicks() {
        return ticks;
    }

    public String getName() {
        return name;
    }

    public List<ItemStack> getRequiredItemStacks() {
        List<ItemStack> result = new ArrayList<>();
        for(String ref : reference.keySet()) {
            IBlockState state = reference.get(ref);
            int count = referenceCount.getOrDefault(ref, 0);
            if(count == 0) {
                continue;
            }

            if(state.getBlock() == Blocks.REDSTONE_WIRE) {
                result.add(new ItemStack(Items.REDSTONE, count));
            } else {
                result.add(new ItemStack(state.getBlock(), count, state.getBlock().getMetaFromState(state)));
            }
        }

        return result;
    }

    public void setPositionMap(String[][][] map) {
        this.map = map;

        // Prerotate the maps, so we do this only when the recipes are being loaded
        // and not with every check whether a recipe is valid.
        if(!symmetrical) {
            this.map90 = this.rotateMapCW(this.map);
            this.map180 = this.rotateMapCW(this.map90);
            this.map270 = this.rotateMapCW(this.map180);
        }

        // Count blocks, we can use this to quickly skip this recipe if the crafting
        // area is of a different size.
        this.mapAsBlockPos = new ArrayList<>();
        this.count = 0;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int minX = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int maxX = Integer.MIN_VALUE;
        for(int y = 0; y < map.length; y++) {
            for(int z = 0; z < map[y].length; z++) {
                for(int x = 0; x < map[y][z].length; x++) {
                    String content = map[y][z][x];
                    if(content.equals("_")) {
                        continue;
                    }

                    referenceCount.put(content, referenceCount.getOrDefault(content, 0)+1);

                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    minX = Math.min(minX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                    maxX = Math.max(maxX, x);
                    mapAsBlockPos.add(new BlockPos(x, y, z));
                    this.count++;
                }
            }
        }

        this.minPos = new BlockPos(minX, minY, minZ);
        this.maxPos = new BlockPos(maxX, maxY, maxZ);
    }

    public IBlockAccess getBlockAccess() {
        return new IBlockAccess() {
            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                return null;
            }

            @Override
            public int getCombinedLight(BlockPos pos, int lightValue) {
                return 255;
            }

            @Override
            public IBlockState getBlockState(BlockPos pos) {
                return getStateAtBlockPos(pos);
            }

            @Override
            public boolean isAirBlock(BlockPos pos) {
                IBlockState blockState = this.getBlockState(pos);
                return blockState.getBlock().isAir(blockState, this, pos);
            }

            @Override
            public Biome getBiome(BlockPos pos) {
                return Biomes.PLAINS;
            }

            @Override
            public int getStrongPower(BlockPos pos, EnumFacing direction) {
                return 0;
            }

            @Override
            public WorldType getWorldType() {
                return WorldType.FLAT;
            }

            @Override
            public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
                return this.getBlockState(pos).isSideSolid(this, pos, side);
            }
        };
    }

    public boolean tryCrafting(World world, List<BlockPos> insideBlocks) {
        return tryCrafting(world, insideBlocks, null);
    }

    public boolean tryCrafting(World world, List<BlockPos> insideBlocks, Item item) {
        // If the number of inside blocks does not even match, abort immediately
        if(insideBlocks.size() != this.count) {
            return false;
        }

        // Wrong catalyst -> abort
        if(item != null && item != this.catalyst) {
            return false;
        }

        // Normalize the crafting area to x=0, y=0, z=0. For that we first
        // calculate the "lowest" corner so we can use that to calculate
        // the relative position of the block
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int minX = Integer.MAX_VALUE;
        for(BlockPos pos : insideBlocks) {
            if(pos.getY() < minY) {
                minY = pos.getY();
            }
            if(pos.getZ() < minZ) {
                minZ = pos.getZ();
            }
            if(pos.getX() < minX) {
                minX = pos.getX();
            }
        }

        // If its a symmetrical recipe we only need to test one rotation
        if(symmetrical) {
            return testRotation(world, insideBlocks, minX, minY, minZ, this.map);
        }

        // Test each possible rotation of the crafting recipe
        return (testRotation(world, insideBlocks, minX, minY, minZ, this.map) ||
                testRotation(world, insideBlocks, minX, minY, minZ, this.map90) ||
                testRotation(world, insideBlocks, minX, minY, minZ, this.map180) ||
                testRotation(world, insideBlocks, minX, minY, minZ, this.map270)
        );
    }

    private boolean testRotation(World world, List<BlockPos> insideBlocks, int minX, int minY, int minZ, String[][][] map) {
        // Perform a few tests for each of the inside blocks
        for(BlockPos pos : insideBlocks) {
            BlockPos relativePos = pos.add(-minX, -minY, -minZ);
            int y = relativePos.getY();
            int z = relativePos.getZ();
            int x = relativePos.getX();

            // Test whether the position is outside of the recipe
            if(y < 0 || y >= map.length) {
                return false;
            }
            if(z < 0 || z >= map[y].length) {
                return false;
            }
            if(x < 0 || x >= map[y][z].length) {
                return false;
            }

            // Ignore "_", i.e. air blocks. These are air.
            if(map[map.length - y - 1][z][x].equals("_")) {
                continue;
            }

            // Test whether the block is the type it should be
            IBlockState state = world.getBlockState(pos);
            IBlockState wanted = reference.get(map[y][z][x]);
            if(wanted == null || state.getBlock() != wanted.getBlock() || state.getBlock().getMetaFromState(state) != wanted.getBlock().getMetaFromState(wanted)) {
                return false;
            }
        }

        return true;
    }

    private String[][][] rotateMapCW(String[][][] map) {
        String[][][] ret = new String[map.length][][];
        for(int y = 0; y < map.length; y++) {
            final int M = map[y].length;
            final int N = map[y][0].length;
            String[][] slice = new String[N][M];
            for (int r = 0; r < M; r++) {
                for (int c = 0; c < N; c++) {
                    slice[c][M - 1 - r] = map[y][r][c];
                }
            }
            ret[y] = slice;
        }

        return ret;

    }

    public BlockPos getMinPos() {
        return minPos;
    }

    public BlockPos getMaxPos() {
        return maxPos;
    }

    public String getDimensionsString() {
        return String.format("%dx%dx%d", getWidth(), getHeight(), getDepth());
    }

    public int getWidth() {
        return getMaxPos().getX() - getMinPos().getX() +1;
    }

    public int getHeight() {
        return getMaxPos().getY() - getMinPos().getY() +1;
    }

    public int getDepth() {
        return getMaxPos().getZ() - getMinPos().getZ() +1;
    }

    public ItemStack getCatalystStack() {
        return new ItemStack(this.catalyst, 1, this.catalystMeta);
    }

    public ItemStack getTargetStack() {
        return targetStack.copy();
    }

    public IBlockState getStateAtBlockPos(BlockPos pos) {
        if(pos.getY() < 0 || pos.getY() >= this.map.length) {
            return Blocks.AIR.getDefaultState();
        }
        if(pos.getZ() < 0 || pos.getZ() >= this.map[pos.getY()].length) {
            return Blocks.AIR.getDefaultState();
        }
        if(pos.getX() < 0 || pos.getX() >= this.map[pos.getY()][pos.getZ()].length) {
            return Blocks.AIR.getDefaultState();
        }

        String ref = this.map[pos.getY()][pos.getZ()][pos.getX()];
        return reference.getOrDefault(ref, Blocks.AIR.getDefaultState());
    }

    public List<BlockPos> getShapeAsBlockPosList() {
        return this.mapAsBlockPos;
    }
}
