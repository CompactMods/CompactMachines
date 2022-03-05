package dev.compactmods.machines.machine;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CompactMachineItem extends BlockItem {

    public CompactMachineItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public static Optional<Integer> getMachineId(ItemStack stack) {
        if (!stack.hasTag())
            return Optional.empty();

        CompoundTag machineData = stack.getOrCreateTag();
        if (machineData.contains(MachineNbt.ID)) {
            int c = machineData.getInt(MachineNbt.ID);
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
            if(nbt == null)
                return;

            getMachineId(stack).ifPresent(id -> {
                tooltip.add(TranslationUtil.tooltip(Tooltips.Machines.ID, id));
            });

            if (nbt.contains(MachineNbt.OWNER)) {
                UUID owner = nbt.getUUID(MachineNbt.OWNER);
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
            if (b instanceof CompactMachineBlock cmb) {
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
