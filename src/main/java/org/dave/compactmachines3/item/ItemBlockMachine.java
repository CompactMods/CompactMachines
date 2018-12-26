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
import org.dave.compactmachines3.block.BlockMachine;
import org.dave.compactmachines3.block.IMetaBlockName;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.utility.TextFormattingHelper;

import javax.annotation.Nullable;
import java.util.List;

/*
 Incorporates concepts from
   http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/blockstates-and-metadata/
 Thanks! And look there for details!
 */
public class ItemBlockMachine extends ItemBlock {
    public ItemBlockMachine(Block block) {
        super(block);
        if (!(block instanceof IMetaBlockName)) {
            throw new IllegalArgumentException(String.format("The given block %s is not an instance of IMetaBlockName!", block.getTranslationKey()));
        }

        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }


    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + ((IMetaBlockName)this.block).getSpecialName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if(stack.hasTagCompound() && stack.getTagCompound().hasKey("coords")) {
            int coords = stack.getTagCompound().getInteger("coords");
            if(coords > -1) {
                tooltip.add(TextFormattingHelper.colorizeKeyValue(I18n.format("tooltip.compactmachines3.machine.coords") + " #" + coords));
            }
        }

        if(GuiScreen.isShiftKeyDown()) {
            int size = Blockss.machine.getStateFromMeta(stack.getItemDamage()).getValue(BlockMachine.SIZE).getDimension() - 1;
            String sizeString = size + "x" + size + "x" + size;
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.compactmachines3.machine.hint", I18n.format(this.getTranslationKey(stack) + ".name"), sizeString));
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format("tooltip." + CompactMachines3.MODID + ".hold_shift.hint"));
        }
    }
}
