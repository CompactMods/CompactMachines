package dev.compactmods.machines.item;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.block.BlockCompactMachine;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.reference.Reference;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.util.TranslationUtil;
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
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
    }

    public static Optional<Integer> getMachineId(ItemStack stack) {
        if (!stack.hasTag())
            return Optional.empty();

        CompoundNBT machineData = stack.getTagElement("cm");
        if (machineData == null)
            return Optional.empty();

        if (machineData.contains("coords")) {
            int c = machineData.getInt("coords");
            return c > -1 ? Optional.of(c) : Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // We need NBT data for the rest of this
        if (stack.hasTag()) {

            CompoundNBT nbt = stack.getTag();
            assert nbt != null;

            getMachineId(stack).ifPresent(id -> {
                tooltip.add(TranslationUtil.tooltip(Tooltips.Machines.ID, id));
            });

            if (nbt.contains(Reference.CompactMachines.OWNER_NBT)) {
                UUID owner = nbt.getUUID(Reference.CompactMachines.OWNER_NBT);
                Optional<GameProfile> playerProfile = PlayerUtil.getProfileByUUID(worldIn, owner);

                IFormattableTextComponent player = playerProfile
                        .map(p -> (IFormattableTextComponent) new StringTextComponent(p.getName()))
                        .orElse(TranslationUtil.tooltip(Tooltips.UNKNOWN_PLAYER_NAME));

                IFormattableTextComponent ownerText = TranslationUtil.tooltip(Tooltips.Machines.OWNER)
                        .append(player);

                tooltip.add(ownerText);
            }

        }

        if (Screen.hasShiftDown()) {
            Block b = Block.byItem(stack.getItem());
            if (b instanceof BlockCompactMachine) {
                EnumMachineSize size = ((BlockCompactMachine) b).getSize();
                int internalSize = size.getInternalSize();

                IFormattableTextComponent text = TranslationUtil.tooltip(Tooltips.Machines.SIZE, internalSize)
                        .withStyle(TextFormatting.YELLOW);

                tooltip.add(text);
            }
        } else {
            IFormattableTextComponent text = TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                    .withStyle(TextFormatting.DARK_GRAY)
                    .withStyle(TextFormatting.ITALIC);

            tooltip.add(text);
        }
    }
}
