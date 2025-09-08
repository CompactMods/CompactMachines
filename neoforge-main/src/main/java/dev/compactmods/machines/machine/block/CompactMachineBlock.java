package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.network.machine.MachineColorSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    @NotNull
    protected static InteractionResult tryDyingMachine(ServerLevel level, @NotNull BlockPos pos, Player player, DyeItem dye, ItemStack mainItem) {
        // TODO Support IColorable once https://github.com/neoforged/NeoForge/pull/1094 is merged
        var color = dye.getDyeColor();
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICompactMachineBlockEntity cmbe) {
            final var newColor = MachineColor.fromDyeColor(color);
            cmbe.setMachineColor(newColor);

            PacketDistributor.sendToPlayersTrackingChunk(
                    level, new ChunkPos(pos), new MachineColorSyncPacket(GlobalPos.of(level.dimension(), pos), newColor));

            if (!player.isCreative())
                mainItem.shrink(1);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }
}
