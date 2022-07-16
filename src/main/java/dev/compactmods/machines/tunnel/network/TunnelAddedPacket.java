package dev.compactmods.machines.tunnel.network;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.tunnel.client.ClientTunnelHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

public class TunnelAddedPacket {

    @Nonnull
    private final BlockPos position;

    @Nonnull
    private final TunnelDefinition type;

    public TunnelAddedPacket(@Nonnull BlockPos tunnelPos, @Nonnull TunnelDefinition tunnelType) {
        this.position = tunnelPos;
        this.type = tunnelType;
    }

    public TunnelAddedPacket(FriendlyByteBuf buf) {
        position = buf.readBlockPos();
        type = Tunnels.getDefinition(buf.readResourceLocation());
    }

    public static void handle(TunnelAddedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ClientTunnelHandler.setTunnel(message.position, message.type);
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(@Nonnull TunnelAddedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.position);
        buf.writeResourceLocation(Objects.requireNonNull(Tunnels.getRegistryId(pkt.type)));
    }
}
