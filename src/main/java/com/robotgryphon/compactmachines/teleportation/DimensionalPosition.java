package com.robotgryphon.compactmachines.teleportation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.data.codec.CodecExtensions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
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
import java.util.Objects;
import java.util.Optional;

public class DimensionalPosition implements INBTSerializable<CompoundNBT> {

    private RegistryKey<World> dimension;
    private Vector3d position;

    public static final Codec<DimensionalPosition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("dim").forGetter(DimensionalPosition::getDimensionString),
            CodecExtensions.VECTOR3D_CODEC.fieldOf("pos").forGetter(DimensionalPosition::getPosition)
    ).apply(i, DimensionalPosition::new));

    private DimensionalPosition() { }

    private DimensionalPosition(String dimKey, Vector3d pos) {
        this.dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimKey));
        this.position = pos;
    }

    public DimensionalPosition(RegistryKey<World> dim, Vector3d pos) {
        this.dimension = dim;
        this.position = pos;
    }

    public DimensionalPosition(RegistryKey<World> world, BlockPos positionBlock) {
        this.dimension = world;
        this.position = new Vector3d(positionBlock.getX(), positionBlock.getY(), positionBlock.getZ());
    }

    public Optional<ServerWorld> getWorld(@Nonnull MinecraftServer server) {
        return Optional.ofNullable(server.getLevel(this.dimension));
    }

    public static DimensionalPosition fromNBT(CompoundNBT nbt) {
        DimensionalPosition dp = new DimensionalPosition();
        dp.deserializeNBT(nbt);

        return dp;
    }

    @Override
    public CompoundNBT serializeNBT() {
        DataResult<INBT> nbt = CODEC.encodeStart(NBTDynamicOps.INSTANCE, this);
        return (CompoundNBT) nbt.result().orElse(null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        Optional<DimensionalPosition> dimensionalPosition = CODEC
                .parse(NBTDynamicOps.INSTANCE, nbt)
                .resultOrPartial(CompactMachines.LOGGER::error);

        dimensionalPosition.ifPresent(dp -> {
            this.dimension = dp.dimension;
            this.position = dp.position;
        });
    }

    public RegistryKey<World> getDimension() {
        return this.dimension;
    }

    private String getDimensionString() {
        if(this.dimension == null)
            return "";

        return this.dimension.location().toString();
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public BlockPos getBlockPosition() {
        return new BlockPos(position.x, position.y, position.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionalPosition that = (DimensionalPosition) o;
        return Objects.equals(dimension, that.dimension) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, position);
    }

    @Override
    public String toString() {
        return "DimensionalPosition{" +
                "dimension=" + dimension +
                ", position=" + position +
                '}';
    }
}
