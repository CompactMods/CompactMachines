package org.dave.compactmachines3.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import javax.annotation.Nullable;

public class ProxyWorld extends World {
    private final World realWorld;
    private IBlockAccess fakeWorld;

    public ProxyWorld(WorldClient realWorld) {
        super(null, realWorld.getWorldInfo(), realWorld.provider, realWorld.profiler, true);
        this.realWorld = realWorld;
        this.chunkProvider = realWorld.getChunkProvider();
    }

    public ProxyWorld() {
        super(null, Minecraft.getMinecraft().world.getWorldInfo(), Minecraft.getMinecraft().world.provider, Minecraft.getMinecraft().world.profiler, true);
        this.realWorld = Minecraft.getMinecraft().world;
        this.chunkProvider = Minecraft.getMinecraft().world.getChunkProvider();
    }

    public void setFakeWorld(IBlockAccess fakeWorld) {
        this.fakeWorld = fakeWorld;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return realWorld.getChunkProvider();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return x == 0 && z == 0;
    }

    private static BlockPos getFakePos(BlockPos pos) {
        return new BlockPos(pos.getX() % 1024, pos.getY(), pos.getZ());
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if(fakeWorld == null) {
            return super.getBlockState(pos);
        }

        return fakeWorld.getBlockState(getFakePos(pos));
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if(fakeWorld == null) {
            return super.getTileEntity(pos);
        }

        if(pos.getY() >= 40) {
            pos = pos.offset(EnumFacing.DOWN, 40);
        }
        return fakeWorld.getTileEntity(getFakePos(pos));
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return super.isOutsideBuildHeight(getFakePos(pos));
    }



}
