package org.dave.compactmachines3.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.block.IMetaBlockName;
import org.dave.compactmachines3.reference.EnumMachineSize;

import javax.annotation.Nullable;
import java.util.List;

/*
 Incorporates concepts from
   http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/blockstates-and-metadata/
 Thanks! And look there for details!
 */
public class ItemBlockMachine extends BlockItem {

    public ItemBlockMachine(Block blockIn, EnumMachineSize size, Properties builder) {
        super(blockIn, builder);

        if (!(blockIn instanceof IMetaBlockName)) {
            throw new IllegalArgumentException(String.format("The given block %s is not an instance of IMetaBlockName!", blockIn.getTranslationKey()));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (stack.hasTag() && stack.getTag().contains("coords")) {
            int coords = stack.getTag().getInt("coords");
            if (coords > -1) {
                IFormattableTextComponent coordsTC= new TranslationTextComponent("tooltip.compactmachines3.machine.coords")
                        .append(new StringTextComponent(" #" + coords));

                tooltip.add(coordsTC);
            }
        }

        if (false) {
            // TODO Show size information when sneaking
            // int size = Blockss.machine.getStateFromMeta(stack.getItemDamage()).getValue(BlockMachine.SIZE).getDimension() - 1;
            // String sizeString = size + "x" + size + "x" + size;
            // tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.compactmachines3.machine.hint", I18n.format(this.getTranslationKey(stack) + ".name"), sizeString));
        } else {
            IFormattableTextComponent text = new StringTextComponent("" + TextFormatting.GRAY)
                    .append(new TranslationTextComponent("tooltip." + CompactMachines3.MODID + ".hold_shift.hint"));

            tooltip.add(text);
        }
    }
}
