package dev.compactmods.machines.room.block;

import dev.compactmods.machines.core.Translations;
import dev.compactmods.machines.core.WallConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class ItemBlockWall extends BlockItem {

    public ItemBlockWall(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        if (stack.is(WallConstants.TAG_SOLID_WALL_ITEMS)) {
            tooltipAdder.accept(flag.hasShiftDown() ?
                    ProtectedWallBlock.COMPONENT_UNBREAKABLE_BLOCK.get() : Translations.HINT_HOLD_SHIFT.get());
        }
    }
}
