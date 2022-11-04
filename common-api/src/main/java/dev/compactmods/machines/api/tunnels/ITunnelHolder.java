package dev.compactmods.machines.api.tunnels;

public interface ITunnelHolder {

    TunnelDefinition getTunnelType();

    void setTunnelType(TunnelDefinition type);
}
