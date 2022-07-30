package dev.compactmods.machines.room.network;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.dimension.MissingDimensionException;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRequestedTeleportPacket(LevelBlockPosition machine, ChunkPos room) {

    public PlayerRequestedTeleportPacket(FriendlyByteBuf buf) {
        this(buf.readWithCodec(LevelBlockPosition.CODEC), buf.readChunkPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeWithCodec(LevelBlockPosition.CODEC, machine);
        buf.writeChunkPos(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final var player = ctx.get().getSender();
            try {
                PlayerUtil.teleportPlayerIntoMachine(player.level, player, machine.getBlockPosition());
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.error("Failed to teleport player into machine.", e);
            }
        });

        return true;
    }
}
