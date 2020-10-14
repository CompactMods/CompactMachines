package com.robotgryphon.compactmachines.item;

import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockWall extends BlockItem {

    public ItemBlockWall(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (stack.getItem() == Registrations.ITEM_WALL.get()) {
            IFormattableTextComponent text = new TranslationTextComponent("tooltip.compactmachines.wall.hint")
                    .mergeStyle(TextFormatting.RED);
            
            tooltip.add(text);
        }

    }
}
