package dev.compactmods.machines.compat.theoneprobe;

import dev.compactmods.machines.CompactMachines;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class TheOneProbeProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return CompactMachines.MOD_ID;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {
        if (blockState.getBlock() instanceof IProbeDataProvider) {
            ((IProbeDataProvider) blockState.getBlock()).addProbeData(
                    new ProbeData(info, probeMode, iProbeHitData),
                    playerEntity,
                    world,
                    blockState
            );
        }
    }
}
