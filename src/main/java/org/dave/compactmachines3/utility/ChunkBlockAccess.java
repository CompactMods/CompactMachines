package org.dave.compactmachines3.utility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

import static net.minecraft.world.chunk.Chunk.EnumCreateEntityType.IMMEDIATE;

public class ChunkBlockAccess implements IBlockAccess {
    protected Chunk chunk;

    public ChunkBlockAccess(Chunk chunk) {
        this.chunk = chunk;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        pos = pos.add(0, 40, 0);
        return chunk.getTileEntity(pos, IMMEDIATE);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 15;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        pos = pos.add(0, 40, 0);
        return chunk.getBlockState(pos);
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
}
