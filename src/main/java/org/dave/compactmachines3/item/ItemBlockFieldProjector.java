package org.dave.compactmachines3.item;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.dave.compactmachines3.CompactMachines3;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockFieldProjector extends ItemBlock {
    public ItemBlockFieldProjector(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if(GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip." + CompactMachines3.MODID + ".fieldprojector.hint"));
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip." + CompactMachines3.MODID + ".hold_shift.hint"));
        }
    }
}
