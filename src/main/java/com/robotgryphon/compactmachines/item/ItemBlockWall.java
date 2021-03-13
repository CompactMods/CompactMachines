package com.robotgryphon.compactmachines.item;

import com.robotgryphon.compactmachines.core.Registration;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class ItemBlockWall extends BlockItem {

    public ItemBlockWall(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (stack.getItem() == Registration.ITEM_SOLID_WALL.get()) {
            IFormattableTextComponent text = new TranslationTextComponent("tooltip.compactmachines.solid_wall.hint")
                    .withStyle(TextFormatting.RED);
            
            tooltip.add(text);
        }

    }
}
