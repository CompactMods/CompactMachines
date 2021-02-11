package com.robotgryphon.compactmachines.teleportation;

import com.robotgryphon.compactmachines.data.NbtDataUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class DimensionalPosition implements INBTSerializable<CompoundNBT> {

    private RegistryKey<World> dimension;
    private Vector3d position;

    private DimensionalPosition() { }

    public DimensionalPosition(RegistryKey<World> dim, Vector3d pos) {
        this.dimension = dim;
        this.position = pos;
    }

    public DimensionalPosition(RegistryKey<World> world, BlockPos positionBlock) {
        this.dimension = world;
        this.position = new Vector3d(positionBlock.getX(), positionBlock.getY(), positionBlock.getZ());
    }

    public Optional<ServerWorld> getWorld(@Nonnull MinecraftServer server) {
        return Optional.ofNullable(server.getWorld(this.dimension));
    }

    public static DimensionalPosition fromNBT(CompoundNBT nbt) {
        DimensionalPosition dp = new DimensionalPosition();
        dp.deserializeNBT(nbt);

        return dp;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("dim", dimension.getLocation().toString());
        CompoundNBT posNbt = NbtDataUtil.writeVectorCompound(position);
        nbt.put("pos", posNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("dim"))
        {
            ResourceLocation dim = new ResourceLocation(nbt.getString("dim"));
            this.dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dim);
        }

        if(nbt.contains("pos")) {
            CompoundNBT bPosNbt = nbt.getCompound("pos");
            Vector3d bPos = NbtDataUtil.readVectorCompound(bPosNbt);
            this.position = bPos;
        }
    }

    public RegistryKey<World> getDimension() {
        return this.dimension;
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public BlockPos getBlockPosition() {
        return new BlockPos(position.x, position.y, position.z);
    }

    @Override
    public String toString() {
        return "DimensionalPosition{" +
                "dimension=" + dimension +
                ", position=" + position +
                '}';
    }
}
