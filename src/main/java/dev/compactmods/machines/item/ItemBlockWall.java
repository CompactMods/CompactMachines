package dev.compactmods.machines.item;

import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
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
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (stack.getItem() == Registration.ITEM_SOLID_WALL.get()) {
            IFormattableTextComponent text;
            if (Screen.hasShiftDown()) {
                text = TranslationUtil.tooltip(Tooltips.Details.SOLID_WALL)
                        .withStyle(TextFormatting.DARK_RED);
            } else {
                text = TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                        .withStyle(TextFormatting.DARK_GRAY)
                        .withStyle(TextFormatting.ITALIC);
            }

            tooltip.add(text);
        }

    }
}
