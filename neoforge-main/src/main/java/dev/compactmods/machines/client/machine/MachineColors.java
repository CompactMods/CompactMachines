package dev.compactmods.machines.client.machine;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineColors {

    public static final ResourceLocation ITEM_PROVIDER_ID = CompactMachines.modRL("machine_color");

    private static final int DEFAULT = 0xFFFFFFFF;

    public static class MachineColorComponentItemTintSource implements ItemTintSource {
        public static final MapCodec<ItemTintSource> MAP_CODEC = MapCodec.unit(MachineColorComponentItemTintSource::new);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            if (!stack.is(MachineConstants.MACHINE_ITEM)) return DEFAULT;
            return stack.getOrDefault(CMDataComponents.MACHINE_COLOR, dev.compactmods.machines.machine.MachineColors.WHITE).rgb();
        }

        @Override
        public @NotNull MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }

    public static final BlockColor BLOCK = (state, level, pos, tintIndex) -> {
        if (level == null || pos == null)
            return DEFAULT;

        var be = level.getBlockEntity(pos);
        if (be instanceof ICompactMachineBlockEntity cmbe && tintIndex == 0)
            return cmbe.getMachineColor().rgb();

        return DEFAULT;
    };

    public static void onItemColors(final RegisterColorHandlersEvent.ItemTintSources colors) {
        colors.register(ITEM_PROVIDER_ID, MachineColorComponentItemTintSource.MAP_CODEC);
    }

    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.register(MachineColors.BLOCK, Machines.Blocks.BOUND_MACHINE.get(), Machines.Blocks.UNBOUND_MACHINE.get());
    }
}
