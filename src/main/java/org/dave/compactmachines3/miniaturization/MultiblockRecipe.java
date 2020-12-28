package org.dave.compactmachines3.miniaturization;


import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.dave.compactmachines3.world.ProxyWorld;
import org.dave.compactmachines3.world.data.provider.AbstractExtraTileDataProvider;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProviderRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockRecipe {
    private String name;

    private String[][][] variantMap;
    private String[][][] map;
    private String[][][] map90;
    private String[][][] map180;
    private String[][][] map270;
    private List<BlockPos> mapAsBlockPos;
    private Map<String, IBlockState> reference;
    private Map<String, Integer> referenceCount;
    private Map<String, Boolean> referenceIgnoresMeta;
    private Map<String, NBTTagCompound> referenceTags;
    //private Map<String, Boolean> referenceIgnoresNBT;
    private Map<String, ItemStack> referenceStacks;

    private BlockPos minPos;
    private BlockPos maxPos;

    private ItemStack targetStack;

    private Item catalyst;
    private int catalystMeta;
    private NBTTagCompound catalystNbt;
    private int count;

    private int ticks;

    private boolean symmetrical;

    public MultiblockRecipe(String name, ItemStack targetStack, Item catalyst, int catalystMeta, NBTTagCompound catalystNbt, boolean symmetrical, int ticks) {
        this.name = name;
        this.reference = new HashMap<>();
        this.referenceCount = new HashMap<>();
        this.referenceIgnoresMeta = new HashMap<>();
        this.referenceTags = new HashMap<>();
        //this.referenceIgnoresNBT = new HashMap<>();
        this.referenceStacks = new HashMap<>();
        this.targetStack = targetStack;
        this.catalyst = catalyst;
        this.catalystMeta = catalystMeta;
        this.catalystNbt = catalystNbt;
        this.symmetrical = symmetrical;
        this.ticks = ticks;
    }


    public void addBlockReference(String ref, IBlockState state) {
        this.reference.put(ref, state);
    }

    public void addBlockVariation(String ref, NBTTagCompound tag) {
        this.referenceTags.put(ref, tag);
    }

    public void setIgnoreMeta(String ref, boolean value) {
        this.referenceIgnoresMeta.put(ref, value);
    }

    /*public void setIgnoreNBT(String ref, boolean value) {
        this.referenceIgnoresNBT.put(ref, value);
    }*/

    public void setReferenceStack(String ref, ItemStack stack) {
        this.referenceStacks.put(ref, stack);
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

            if(referenceStacks.containsKey(ref)) {
                result.add(referenceStacks.get(ref).copy());
            } else if(state.getBlock() == Blocks.REDSTONE_WIRE) {
                // TODO: 1.13: We keep this in for historic reasons. Remove in 1.13
                result.add(new ItemStack(Items.REDSTONE, count));
            } else {
                if(referenceIgnoresMeta.getOrDefault(ref, false)) {
                    result.add(new ItemStack(state.getBlock(), count, 0));
                } else {
                    result.add(new ItemStack(state.getBlock(), count, state.getBlock().getMetaFromState(state)));
                }
            }
        }

        return result;
    }

    public void setVariantMap(String[][][] variantMap) {
        this.variantMap = variantMap;
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

                    referenceCount.merge(content, 1, Integer::sum);

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

    public IBlockAccess getBlockAccess(ProxyWorld proxyWorld) {
        return new IBlockAccess() {
            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                IBlockState state = getBlockState(pos);
                if(state.getBlock().hasTileEntity(state)) {
                    TileEntity tileentity = state.getBlock().createTileEntity(proxyWorld, state);
                    if (tileentity != null) {
                        tileentity.setWorld(proxyWorld);
                        NBTTagCompound nbt = getVariantAtBlockPos(pos);
                        if(nbt != null) {
                            tileentity.readFromNBT(nbt);
                            for (AbstractExtraTileDataProvider provider : ExtraTileDataProviderRegistry.getDataProviders(tileentity)) {
                                String tagName = String.format("cm3_extra:%s", provider.getName());
                                if (nbt.hasKey(tagName)) {
                                    provider.readExtraData(tileentity, (NBTTagCompound) nbt.getTag(tagName));
                                }
                            }
                        }
                    }

                    return tileentity;
                }

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

    public boolean tryCrafting(World world, List<BlockPos> insideBlocks, ItemStack itemStack) {
        // If the number of inside blocks does not even match, abort immediately
        if(insideBlocks.size() != this.count) {
            return false;
        }

        // Wrong catalyst -> abort
        if(itemStack != null) {
            boolean itemMatches = this.catalyst == itemStack.getItem();
            boolean metaMatches = this.catalystMeta == itemStack.getMetadata();
            boolean nbtMatches = this.catalystNbt == null || NBTUtil.areNBTEquals(this.catalystNbt, itemStack.getTagCompound(), false);

            if(!(itemMatches && metaMatches && nbtMatches)) {
                return false;
            }
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
            IBlockState state = world.getBlockState(pos);
            if(map[map.length - y - 1][z][x].equals("_") && state.getBlock().isAir(state, world, pos)) {
                continue;
            }

            // Test whether the block is the type it should be
            IBlockState wanted = reference.get(map[y][z][x]);
            if(wanted == null || state.getBlock() != wanted.getBlock()) {
                return false;
            }

            if(!referenceIgnoresMeta.getOrDefault(map[y][z][x], false)) {
                if(state.getBlock().getMetaFromState(state) != wanted.getBlock().getMetaFromState(wanted)) {
                    return false;
                }
            }

            /*
            if(!referenceIgnoresNBT.getOrDefault(map[y][z][x], true)) {
                NBTTagCompound wantedTag = referenceTags.getOrDefault(map[y][z][x], null);

                TileEntity tileEntity = world.getTileEntity(pos);
                if(tileEntity == null) {
                    if(wantedTag != null) {
                        return false;
                    }
                } else {
                    NBTTagCompound actualTag = new NBTTagCompound();
                    tileEntity.writeToNBT(actualTag);

                    if(!NBTUtil.areNBTEquals(actualTag, wantedTag, true)) {
                        return false;
                    }
                }
            }
            */
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
        ItemStack result = new ItemStack(this.catalyst, 1, this.catalystMeta);
        if(this.catalystNbt != null) {
            result.setTagCompound(this.catalystNbt.copy());
        }
        return result;
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

    public NBTTagCompound getVariantAtBlockPos(BlockPos pos) {
        if(pos.getY() < 0 || pos.getY() >= this.map.length) {
            return null;
        }
        if(pos.getZ() < 0 || pos.getZ() >= this.map[pos.getY()].length) {
            return null;
        }
        if(pos.getX() < 0 || pos.getX() >= this.map[pos.getY()][pos.getZ()].length) {
            return null;
        }

        String variant = this.variantMap[pos.getY()][pos.getZ()][pos.getX()];
        return this.referenceTags.getOrDefault(variant, null);
    }

    public List<BlockPos> getShapeAsBlockPosList() {
        return this.mapAsBlockPos;
    }
}
