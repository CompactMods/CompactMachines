package dev.compactmods.machines.room.network;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRequestedTeleportPacket(GlobalPos machine, ChunkPos room) {

    public PlayerRequestedTeleportPacket(FriendlyByteBuf buf) {
        this(buf.readWithCodec(GlobalPos.CODEC), buf.readChunkPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeWithCodec(GlobalPos.CODEC, machine);
        buf.writeChunkPos(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final var player = ctx.get().getSender();
            try {
                PlayerUtil.teleportPlayerIntoMachine(player.level, player, machine.pos());
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.error("Failed to teleport player into machine.", e);
            }
        });

        return true;
    }
}
