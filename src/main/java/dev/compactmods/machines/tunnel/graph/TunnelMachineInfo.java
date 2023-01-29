package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;

public record TunnelMachineInfo(BlockPos location, ResourceLocation type, GlobalPos machine, Direction side) {

    public static final Codec<TunnelMachineInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("location").forGetter(TunnelMachineInfo::location),
            ResourceLocation.CODEC.fieldOf("type").forGetter(TunnelMachineInfo::type),
            GlobalPos.CODEC.fieldOf("machine").forGetter(TunnelMachineInfo::machine),
            Direction.CODEC.fieldOf("side").forGetter(TunnelMachineInfo::side)
    ).apply(i, TunnelMachineInfo::new));
}
