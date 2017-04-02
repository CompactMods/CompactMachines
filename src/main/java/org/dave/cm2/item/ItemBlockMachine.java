package org.dave.cm2.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.dave.cm2.block.IMetaBlockName;

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
                // TODO: Localization
                tooltip.add("Machine: " + coords);
            }
        }
    }
}
