package org.dave.compactmachines3.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;

import java.util.List;

public class ItemTunnelTool extends ItemBase {
    public ItemTunnelTool() {
        super();

        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);

        if(GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip." + CompactMachines3.MODID + ".tunneltool.hint"));
        }
    }
}
