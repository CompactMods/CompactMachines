package org.dave.cm2.item;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.dave.cm2.block.BlockMachine;
import org.dave.cm2.block.IMetaBlockName;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.utility.TextFormattingHelper;

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
            throw new IllegalArgumentException(String.format("The given block %s is not an instance of IMetaBlockName!", block.getUnlocalizedName()));
        }

        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + ((IMetaBlockName)this.block).getSpecialName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);

        if(stack.hasTagCompound() && stack.getTagCompound().hasKey("coords")) {
            int coords = stack.getTagCompound().getInteger("coords");
            if(coords > -1) {
                tooltip.add(TextFormattingHelper.colorizeKeyValue(I18n.format("tooltip.cm2.machine.coords", "#" + coords)));
            }
        }

        if(GuiScreen.isShiftKeyDown()) {
            int size = Blockss.machine.getStateFromMeta(stack.getItemDamage()).getValue(BlockMachine.SIZE).getDimension() - 1;
            String sizeString = size + "x" + size + "x" + size;
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.cm2.machine.hint", I18n.format(this.getUnlocalizedName(stack) + ".name"), sizeString));

        }
    }
}
