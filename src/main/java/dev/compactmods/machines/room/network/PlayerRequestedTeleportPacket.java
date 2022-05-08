package dev.compactmods.machines.room.network;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.CompactMachinesNet;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record PlayerRequestedTeleportPacket(int machine, ChunkPos room) {

    public PlayerRequestedTeleportPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readChunkPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(machine);
        buf.writeChunkPos(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final var player = ctx.get().getSender();
            try {
                Machines.location(player.server, machine).ifPresent(dp -> {
                    try {
                        PlayerUtil.teleportPlayerIntoMachine(player.level, player, dp.getBlockPosition());
                    } catch (MissingDimensionException e) {
                        CompactMachines.LOGGER.error("Failed to teleport player into machine.", e);
                    }
                });
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.error("Failed to teleport player into machine: compact dimension not found.", e);
            }
        });

        return true;
    }
}
