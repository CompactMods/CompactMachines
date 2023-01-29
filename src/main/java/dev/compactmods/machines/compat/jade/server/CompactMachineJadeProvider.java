package dev.compactmods.machines.compat.jade.server;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.compat.CompactMachineTooltipData;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompactMachineJadeProvider implements IServerDataProvider<BlockEntity> {
    public static final CompactMachineJadeProvider INSTANCE = new CompactMachineJadeProvider();

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity entity, boolean showDetails) {
        if(entity instanceof CompactMachineBlockEntity machine)
           appendData(tag, player, machine);
    }

    public void appendData(CompoundTag tag, ServerPlayer serverPlayer, CompactMachineBlockEntity machine) {
        final var compactDim = serverPlayer.server.getLevel(Registration.COMPACT_DIMENSION);

        final var tooltipData = CompactMachineTooltipData.forMachine(compactDim, machine);
        final var tooltipNbt = CompactMachineTooltipData.CODEC.encodeStart(NbtOps.INSTANCE, tooltipData)
                .getOrThrow(false, CompactMachines.LOGGER::error);

        if(tooltipNbt instanceof CompoundTag ct) {
            tag.merge(ct);
        }
    }
}
