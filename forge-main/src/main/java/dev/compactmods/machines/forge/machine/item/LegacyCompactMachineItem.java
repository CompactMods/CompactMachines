package dev.compactmods.machines.forge.machine.item;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.Registries;
import dev.compactmods.machines.forge.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "5.2.0")
public class LegacyCompactMachineItem extends BlockItem implements ICompactMachineItem {
    public static final TagKey<Item> TAG = TagKey.create(Registries.ITEMS.getRegistryKey(),
            new ResourceLocation(Constants.MOD_ID, "legacy_machines"));;

    public LegacyCompactMachineItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public static Item getItemBySize(RoomSize size) {
        return switch (size) {
            case TINY -> Machines.MACHINE_BLOCK_ITEM_TINY.get();
            case SMALL -> Machines.MACHINE_BLOCK_ITEM_SMALL.get();
            case NORMAL -> Machines.MACHINE_BLOCK_ITEM_NORMAL.get();
            case LARGE -> Machines.MACHINE_BLOCK_ITEM_LARGE.get();
            case GIANT -> Machines.MACHINE_BLOCK_ITEM_GIANT.get();
            case MAXIMUM -> Machines.MACHINE_BLOCK_ITEM_MAXIMUM.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(TranslationUtil.tooltip(Tooltips.CRAFT_TO_UPGRADE).withStyle(ChatFormatting.YELLOW));

        boolean sneaking = Screen.hasShiftDown();

        if (sneaking) {
            Block b = Block.byItem(stack.getItem());
            if (b instanceof LegacySizedCompactMachineBlock cmb) {
                RoomSize size = cmb.getSize();
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
