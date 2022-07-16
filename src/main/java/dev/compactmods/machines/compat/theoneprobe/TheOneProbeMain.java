package dev.compactmods.machines.compat.theoneprobe;

import dev.compactmods.machines.compat.theoneprobe.overrides.CompactMachineNameOverride;
import dev.compactmods.machines.compat.theoneprobe.providers.CompactMachineProvider;
import dev.compactmods.machines.compat.theoneprobe.providers.TunnelProvider;
import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

public class TheOneProbeMain implements Function<Object, Void> {
    static ITheOneProbe PROBE;

    @Override
    public Void apply(Object o) {
        PROBE = (ITheOneProbe) o;
        PROBE.registerBlockDisplayOverride(new CompactMachineNameOverride());
        PROBE.registerProvider(new CompactMachineProvider());
        PROBE.registerProvider(new TunnelProvider());

        return null;
    }

}
