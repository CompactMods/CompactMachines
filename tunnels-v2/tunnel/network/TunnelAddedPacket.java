package dev.compactmods.machines.neoforge.tunnel.network;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.tunnel.client.ClientTunnelHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.neoforged.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public record TunnelAddedPacket(BlockPos position, ResourceKey<TunnelDefinition> type) {

    public TunnelAddedPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readResourceKey(TunnelDefinition.REGISTRY_KEY));
    }

    public static void handle(TunnelAddedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            final var holder = ClientTunnelHandler.getTunnelHolder(message.position);
            holder.setTunnelType(message.type);
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(@Nonnull TunnelAddedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.position);
        buf.writeResourceKey(pkt.type);
    }
}
