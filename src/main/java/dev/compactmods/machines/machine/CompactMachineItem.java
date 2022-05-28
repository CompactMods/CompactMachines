package dev.compactmods.machines.machine;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CompactMachineItem extends BlockItem {

    private static final String ROOM_NBT = "room_pos";

    public CompactMachineItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Deprecated(forRemoval = true)
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

    public static Item getItemBySize(RoomSize size) {
        return switch (size) {
            case TINY -> Registration.MACHINE_BLOCK_ITEM_TINY.get();
            case SMALL -> Registration.MACHINE_BLOCK_ITEM_SMALL.get();
            case NORMAL -> Registration.MACHINE_BLOCK_ITEM_NORMAL.get();
            case LARGE -> Registration.MACHINE_BLOCK_ITEM_LARGE.get();
            case GIANT -> Registration.MACHINE_BLOCK_ITEM_GIANT.get();
            case MAXIMUM -> Registration.MACHINE_BLOCK_ITEM_MAXIMUM.get();
        };
    }

    public static Optional<ChunkPos> getRoom(ItemStack stack) {
        if (!stack.hasTag())
            return Optional.empty();

        var tag = stack.getTag();
        if(!tag.contains(ROOM_NBT)) {
            return Optional.empty();
        }

        var roomNbt = tag.getIntArray(ROOM_NBT);
        return Optional.of(new ChunkPos(roomNbt[0], roomNbt[1]));
    }

    public static void setRoom(ItemStack stack, ChunkPos room) {
        var tag = stack.getOrCreateTag();
        tag.putIntArray(ROOM_NBT, new int[] { room.x, room.z });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // We need NBT data for the rest of this
        if (stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            if(nbt == null)
                return;

            // Try room binding; if failed, try old machine ID binding
            getRoom(stack).ifPresentOrElse(room -> {
                // TODO - Server-synced room name list
                tooltip.add(TranslationUtil.tooltip(Tooltips.ROOM_NAME, room));
            }, () -> {
                getMachineId(stack).ifPresent(id -> {
                    tooltip.add(TranslationUtil.tooltip(Tooltips.Machines.ID, id));
                });
            });
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
