package com.robotgryphon.compactmachines.client;

import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.item.TunnelItem;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class TunnelItemColor implements IItemColor {
    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        Optional<TunnelDefinition> definition = TunnelItem.getDefinition(stack);
        if(!definition.isPresent())
            return 0;

        TunnelDefinition actualDef = definition.get();
        if (tintIndex == 0) {
            return actualDef.getTunnelRingColor();
        }

        return actualDef.getTunnelIndicatorColor();
    }
}
