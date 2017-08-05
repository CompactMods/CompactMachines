package org.dave.compactmachines3.item;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ItemBlockWall extends ItemBlock {
    public ItemBlockWall(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);

        tooltip.add(TextFormatting.RED + I18n.format("tooltip.compactmachines3.wall.hint"));
    }
}
