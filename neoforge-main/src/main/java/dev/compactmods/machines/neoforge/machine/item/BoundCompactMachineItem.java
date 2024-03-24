package dev.compactmods.machines.neoforge.machine.item;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.Tooltips;
import dev.compactmods.machines.api.machine.MachineCreator;
import dev.compactmods.machines.api.machine.item.IBoundCompactMachineItem;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.api.machine.item.ICompactMachineItem;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.machine.Machines;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BoundCompactMachineItem extends BlockItem implements IBoundCompactMachineItem {

    public static final String NBT_ROOM_DIMENSIONS = "room_dimensions";

    private static final String FALLBACK_ID = Util.makeDescriptionId("block", CompactMachines.rl("bound_machine_fallback"));

    public BoundCompactMachineItem(Properties builder) {
        super(Machines.MACHINE_BLOCK.get(), builder);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getMachineName(stack)
                .map(Component::literal)
                .orElse(Component.translatableWithFallback(FALLBACK_ID, "Compact Machine"));
    }

    @NotNull
    @Override
    public String getDescriptionId(ItemStack stack) {
        return FALLBACK_ID;
    }

    public static Vec3i getRoomSize(ItemStack stack) {
        if (!stack.hasTag()) return Vec3i.ZERO;
        final var tag = stack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(NBT_ROOM_DIMENSIONS)) return Vec3i.ZERO;
        final var dimNbt = tag.getIntArray(NBT_ROOM_DIMENSIONS);
        return new Vec3i(dimNbt[0], dimNbt[1], dimNbt[2]);
    }

    public static ItemStack setRoomSize(ItemStack stack, Vec3i innerBounds) {
        var tag = stack.getOrCreateTag();
        tag.putIntArray(NBT_ROOM_DIMENSIONS, new int[]{
                innerBounds.getX(),
                innerBounds.getY(),
                innerBounds.getZ()
        });

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        getRoom(stack).ifPresent(room -> {
            // TODO - Server-synced room name list
            tooltip.add(TranslationUtil.tooltip(Tooltips.ROOM_NAME, room));
        });
    }
}
