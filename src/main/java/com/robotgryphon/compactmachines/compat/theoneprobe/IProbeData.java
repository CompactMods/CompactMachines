package com.robotgryphon.compactmachines.compat.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

public interface IProbeData {
    ProbeMode getMode();
    IProbeInfo getInfo();
    IProbeHitData getHitData();
}
