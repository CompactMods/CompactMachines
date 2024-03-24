package dev.compactmods.machines.neoforge.compat.jade;

import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlock;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CMJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CompactMachineJadeProvider.INSTANCE, BoundCompactMachineBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CompactMachineJadeProvider.INSTANCE, BoundCompactMachineBlock.class);
    }

}
