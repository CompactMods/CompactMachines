package dev.compactmods.machines.room;

import javax.annotation.Nullable;
import java.util.List;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class ItemBlockWall extends BlockItem {

    public ItemBlockWall(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (stack.getItem() == Registration.ITEM_SOLID_WALL.get()) {
            MutableComponent text;
            if (Screen.hasShiftDown()) {
                text = TranslationUtil.tooltip(Tooltips.Details.SOLID_WALL)
                        .withStyle(ChatFormatting.DARK_RED);
            } else {
                text = TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .withStyle(ChatFormatting.ITALIC);
            }

            tooltip.add(text);
        }

    }
}
