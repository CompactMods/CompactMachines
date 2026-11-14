package dev.compactmods.machines.machine.client;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MachineColors {

    public static final Identifier ITEM_PROVIDER_ID = CompactMachinesCore.identifier("machine_color");

    private static final int DEFAULT = 0xFFFFFFFF;

    public static class MachineColorComponentItemTintSource implements ItemTintSource {
        public static final MapCodec<ItemTintSource> MAP_CODEC = MapCodec.unit(MachineColorComponentItemTintSource::new);

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            if (!stack.is(MachineConstants.MACHINE_ITEM)) return DEFAULT;
            return stack.getOrDefault(Machines.DataComponents.MACHINE_COLOR, MachineColor.WHITE).rgb();
        }

        @Override
        public @NotNull MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }

    public static final BlockTintSource BLOCK = new BlockTintSource() {
        @Override
        public int color(@NonNull BlockState blockState) {
            return DEFAULT;
        }

        @Override
        public int colorAsTerrainParticle(@NonNull BlockState state, BlockAndTintGetter level, @NonNull BlockPos pos) {
            return worldColor(level, pos);
        }

        @Override
        public int colorInWorld(@NonNull BlockState state, BlockAndTintGetter level, @NonNull BlockPos pos) {
            return worldColor(level, pos);
        }

        private int worldColor(BlockAndTintGetter level, @NonNull BlockPos pos) {
            var be = level.getBlockEntity(pos);
            if (be instanceof ICompactMachineBlockEntity cmbe)
                return cmbe.getMachineColor().rgb();

            return DEFAULT;
        }
    };

    public static void onItemColors(final RegisterColorHandlersEvent.ItemTintSources colors) {
        colors.register(ITEM_PROVIDER_ID, MachineColorComponentItemTintSource.MAP_CODEC);
    }

    public static void onBlockColors(final RegisterColorHandlersEvent.BlockTintSources colors) {
        colors.register(List.of(MachineColors.BLOCK), Machines.Blocks.MACHINE.get());
    }
}
