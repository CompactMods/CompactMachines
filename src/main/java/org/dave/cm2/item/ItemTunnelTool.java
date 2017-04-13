package org.dave.cm2.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.misc.CreativeTabCM2;

import java.util.List;

public class ItemTunnelTool extends ItemBase {
    public ItemTunnelTool() {
        super();

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);

        if(GuiScreen.isShiftKeyDown()) {
            tooltip.add(I18n.format("tooltip." + CompactMachines2.MODID + ".tunneltool.hint"));
        }
    }
}
