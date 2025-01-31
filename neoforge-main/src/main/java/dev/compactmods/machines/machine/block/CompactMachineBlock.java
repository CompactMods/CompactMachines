package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.machine.MachineColorSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompactMachineBlock extends Block {
    public CompactMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(level, pPos, pState, pPlacer, pStack);

        final var color = pStack.getOrDefault(CMDataComponents.MACHINE_COLOR, MachineColors.WHITE);
        final var be = level.getBlockEntity(pPos);
        if(be != null)
            be.setData(CMDataAttachments.MACHINE_COLOR, color);
    }

    @NotNull
    protected static ItemInteractionResult tryDyingMachine(ServerLevel level, @NotNull BlockPos pos, Player player, DyeItem dye, ItemStack mainItem) {
        // TODO Support IColorable once https://github.com/neoforged/NeoForge/pull/1094 is merged
        var color = dye.getDyeColor();
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICompactMachineBlockEntity) {
            final var newColor = MachineColor.fromDyeColor(color);
            blockEntity.setData(CMDataAttachments.MACHINE_COLOR, newColor);

            PacketDistributor.sendToPlayersTrackingChunk(
                    level, new ChunkPos(pos), new MachineColorSyncPacket(GlobalPos.of(level.dimension(), pos), newColor));

            if (!player.isCreative())
                mainItem.shrink(1);

            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.FAIL;
    }
}
