package com.robotgryphon.compactmachines.compat.theoneprobe;

import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

public class TheOneProbeMain implements Function<Object, Void> {
    static ITheOneProbe PROBE;

    @Override
    public Void apply(Object o) {
        PROBE = (ITheOneProbe) o;
        PROBE.registerProvider(new TheOneProbeProvider());

        return null;
    }
}
