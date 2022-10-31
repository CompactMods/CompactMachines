package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.Tooltips;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.registration.IBasicRoomInfo;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BoundCompactMachineItem extends CompactMachineItem {
    public static final String ROOM_NBT = "room_pos";
    public static final String ROOM_DIMENSIONS_NBT = "room_dimensions";

    public BoundCompactMachineItem(Block blockIn, Properties builder) {
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

    public static Optional<String> getRoom(ItemStack stack) {
        if (!stack.hasTag())
            return Optional.empty();

        var tag = stack.getTag();
        if (tag == null || !tag.contains(ROOM_NBT))
            return Optional.empty();

        return Optional.of(tag.getString(ROOM_NBT));
    }

    public static void setRoom(ItemStack stack, String room) {
        var tag = stack.getOrCreateTag();
        tag.putString(ROOM_NBT, room);
    }

    public static ItemStack createForRoom(IBasicRoomInfo room) {
        ItemStack item = new ItemStack(Machines.BOUND_MACHINE_BLOCK_ITEM.get());
        setRoom(item, room.code());
        setColor(item, room.color());
        return item;
    }

    public static Vec3i getRoomSize(ItemStack stack) {
        if (!stack.hasTag()) return Vec3i.ZERO;
        final var tag = stack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(ROOM_DIMENSIONS_NBT)) return Vec3i.ZERO;
        final var dimNbt = tag.getIntArray(ROOM_DIMENSIONS_NBT);
        return new Vec3i(dimNbt[0], dimNbt[1], dimNbt[2]);
    }

    public static ItemStack setRoomSize(ItemStack stack, Vec3i innerBounds) {
        var tag = stack.getOrCreateTag();
        tag.putIntArray(ROOM_DIMENSIONS_NBT, new int[]{
                innerBounds.getX(),
                innerBounds.getY(),
                innerBounds.getZ()
        });

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
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
}
