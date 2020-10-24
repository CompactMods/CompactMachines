package com.robotgryphon.compactmachines.tunnels;

import com.robotgryphon.compactmachines.reference.EnumTunnelType;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TunnelRegistration extends ForgeRegistryEntry<TunnelRegistration> {

    private EnumTunnelType tunnelType;
    private TunnelDefinition definition;

    public TunnelRegistration(EnumTunnelType type, TunnelDefinition def) {
        this.tunnelType = type;
        this.definition = def;
    }

    public TunnelDefinition getDefinition() {
        return definition;
    }

    public EnumTunnelType getType() {
        return tunnelType;
    }
}
