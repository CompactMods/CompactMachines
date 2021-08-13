package dev.compactmods.machines.compat.theoneprobe;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface IProbeDataProvider {
    void addProbeData(IProbeData data, PlayerEntity player, World world, BlockState state);
}
