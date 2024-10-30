package dev.compactmods.machines.compat.jade;

import dev.compactmods.machines.compat.jade.providers.client.CompactMachineProvider;
import dev.compactmods.machines.compat.jade.providers.client.TunnelProvider;
import dev.compactmods.machines.compat.jade.providers.server.CompactMachineComponentProvider;
import dev.compactmods.machines.compat.jade.providers.server.TunnelComponentProvider;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.TunnelWallBlock;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CompactMachineComponentProvider.INSTANCE, CompactMachineBlockEntity.class);
        registration.registerBlockDataProvider(TunnelComponentProvider.INSTANCE, TunnelWallEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CompactMachineProvider.INSTANCE, CompactMachineBlock.class);
        registration.registerBlockComponent(TunnelProvider.INSTANCE, TunnelWallBlock.class);
    }
}
