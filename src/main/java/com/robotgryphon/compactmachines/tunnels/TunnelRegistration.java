package com.robotgryphon.compactmachines.tunnels;

import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class TunnelRegistration extends ForgeRegistryEntry<TunnelRegistration> {

    private TunnelDefinition definition;

    public TunnelRegistration(TunnelDefinition def) {
        this.definition = def;
    }

    @Nonnull
    public TunnelDefinition getDefinition() {
        return definition;
    }
}
