package com.robotgryphon.compactmachines.item;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import com.robotgryphon.compactmachines.reference.Reference;
import com.robotgryphon.compactmachines.util.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ItemBlockMachine extends BlockItem {

    public ItemBlockMachine(Block blockIn, EnumMachineSize size, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        // We need NBT data for the rest of this
        if (!stack.hasTag())
            return;

        CompoundNBT nbt = stack.getTag();
        if (nbt.contains("cm")) {
            CompoundNBT machineData = nbt.getCompound("cm");
            if(machineData.contains("coords")) {
                int coords = machineData.getInt("coords");
                if (coords > -1) {
                    IFormattableTextComponent coordsTC = new TranslationTextComponent("tooltip.compactmachines.machine.coords")
                            .append(new StringTextComponent(" #" + coords));

                    tooltip.add(coordsTC);
                }
            }
        }

        if (nbt.contains(Reference.CompactMachines.OWNER_NBT)) {
            UUID owner = nbt.getUniqueId(Reference.CompactMachines.OWNER_NBT);
            Optional<GameProfile> playerProfile = PlayerUtil.getProfileByUUID(worldIn, owner);

            IFormattableTextComponent player = playerProfile
                    .map(p -> (IFormattableTextComponent) new StringTextComponent(p.getName()))
                    .orElseGet(() -> new TranslationTextComponent("tooltip." + CompactMachines.MODID + ".unknown_player"));

            IFormattableTextComponent ownerText = new TranslationTextComponent("tooltip." + CompactMachines.MODID + ".owner")
                    .append(player);

            tooltip.add(ownerText);
        }

        if (false) {
            // TODO Show size information when sneaking
            // int size = Blockss.machine.getStateFromMeta(stack.getItemDamage()).getValue(BlockMachine.SIZE).getDimension() - 1;
            // String sizeString = size + "x" + size + "x" + size;
            // tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.compactmachines.machine.hint", I18n.format(this.getTranslationKey(stack) + ".name"), sizeString));
        } else {
            IFormattableTextComponent text = new StringTextComponent("" + TextFormatting.GRAY)
                    .append(new TranslationTextComponent("tooltip." + CompactMachines.MODID + ".hold_shift.hint"));

            tooltip.add(text);
        }
    }
}
