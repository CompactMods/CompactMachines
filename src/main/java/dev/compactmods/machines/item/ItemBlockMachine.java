package dev.compactmods.machines.item;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.block.BlockCompactMachine;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.reference.Reference;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item.Properties;

public class ItemBlockMachine extends BlockItem {

    public ItemBlockMachine(Block blockIn, EnumMachineSize size, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
    }

    public static Optional<Integer> getMachineId(ItemStack stack) {
        if (!stack.hasTag())
            return Optional.empty();

        CompoundTag machineData = stack.getTagElement("cm");
        if (machineData == null)
            return Optional.empty();

        if (machineData.contains("coords")) {
            int c = machineData.getInt("coords");
            return c > -1 ? Optional.of(c) : Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // We need NBT data for the rest of this
        if (stack.hasTag()) {

            CompoundTag nbt = stack.getTag();
            assert nbt != null;

            getMachineId(stack).ifPresent(id -> {
                tooltip.add(TranslationUtil.tooltip(Tooltips.Machines.ID, id));
            });

            if (nbt.contains(Reference.CompactMachines.OWNER_NBT)) {
                UUID owner = nbt.getUUID(Reference.CompactMachines.OWNER_NBT);
                Optional<GameProfile> playerProfile = PlayerUtil.getProfileByUUID(worldIn, owner);

                MutableComponent player = playerProfile
                        .map(p -> (MutableComponent) new TextComponent(p.getName()))
                        .orElse(TranslationUtil.tooltip(Tooltips.UNKNOWN_PLAYER_NAME));

                MutableComponent ownerText = TranslationUtil.tooltip(Tooltips.Machines.OWNER)
                        .append(player);

                tooltip.add(ownerText);
            }

        }

        if (Screen.hasShiftDown()) {
            Block b = Block.byItem(stack.getItem());
            if (b instanceof BlockCompactMachine) {
                EnumMachineSize size = ((BlockCompactMachine) b).getSize();
                int internalSize = size.getInternalSize();

                MutableComponent text = TranslationUtil.tooltip(Tooltips.Machines.SIZE, internalSize)
                        .withStyle(ChatFormatting.YELLOW);

                tooltip.add(text);
            }
        } else {
            MutableComponent text = TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .withStyle(ChatFormatting.ITALIC);

            tooltip.add(text);
        }
    }
}
