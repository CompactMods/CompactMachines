package dev.compactmods.machines.compat.jade;

import dev.compactmods.machines.compat.jade.client.CompactMachineJadeComponent;
import dev.compactmods.machines.compat.jade.server.CompactMachineJadeProvider;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class CMJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CompactMachineJadeProvider.INSTANCE, CompactMachineBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(CompactMachineJadeComponent.BODY, TooltipPosition.BODY, CompactMachineBlock.class);
    }

}