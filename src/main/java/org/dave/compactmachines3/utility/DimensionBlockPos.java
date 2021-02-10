package org.dave.compactmachines3.utility;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.world.tools.DimensionTools;

public class DimensionBlockPos  {
    private BlockPos pos;
    private int dimension;

    public DimensionBlockPos(BlockPos pos, int dimension) {
        this.pos = pos;
        this.dimension = dimension;
    }

    public DimensionBlockPos(NBTTagCompound tag) {
        this.pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
        this.dimension = tag.getInteger("dim");
    }

    public DimensionBlockPos(ByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        this.pos = new BlockPos(x, y, z);
        this.dimension = buffer.readInt();
    }

    public BlockPos getBlockPos() { return this.pos; }
    public int getDimension() { return this.dimension; }

    public TileEntity getTileEntity() {
        World world = DimensionTools.getWorldServerForDimension(this.dimension);
        return world == null ? null : world.getTileEntity(this.pos);
    }

    public NBTTagCompound getAsNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
        tag.setInteger("dim", dimension);

        return tag;
    }

    public void writeToByteBuf(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(dimension);
    }
}
