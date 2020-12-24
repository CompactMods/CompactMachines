package com.robotgryphon.compactmachines.item;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.BlockCompactMachine;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import com.robotgryphon.compactmachines.reference.Reference;
import com.robotgryphon.compactmachines.util.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
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
            if (machineData.contains("coords")) {
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
                    .orElseGet(() -> new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".unknown_player"));

            IFormattableTextComponent ownerText = new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".owner")
                    .append(player);

            tooltip.add(ownerText);
        }

        if (Screen.hasShiftDown()) {
            // TODO Show size information when sneaking

            Block b = Block.getBlockFromItem(stack.getItem());
            if (b instanceof BlockCompactMachine) {
                EnumMachineSize size = ((BlockCompactMachine) b).getSize();
                int internalSize = size.getInternalSize();

                IFormattableTextComponent text = new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".machine.size", internalSize)
                        .mergeStyle(TextFormatting.YELLOW);

                tooltip.add(text);
            }
        } else {
            IFormattableTextComponent text = new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".hold_shift.hint")
                    .mergeStyle(TextFormatting.GRAY);

            tooltip.add(text);
        }
    }
}
