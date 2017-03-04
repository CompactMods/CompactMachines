package org.dave.cm2.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemBlockWall extends ItemBlock {
    public ItemBlockWall(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);

        // TODO: Localization
        tooltip.add("Warning! Unbreakable for non-creative players!");
    }
}
