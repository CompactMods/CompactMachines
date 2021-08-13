package dev.compactmods.machines.compat.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

public class ProbeData implements IProbeData {
    private IProbeInfo info;
    private ProbeMode mode;
    private IProbeHitData hitData;

    public ProbeData(IProbeInfo info, ProbeMode mode, IProbeHitData hitData) {
        this.info = info;
        this.mode = mode;
        this.hitData = hitData;
    }

    @Override
    public ProbeMode getMode() {
        return mode;
    }

    @Override
    public IProbeInfo getInfo() {
        return info;
    }

    @Override
    public IProbeHitData getHitData() {
        return hitData;
    }
}
