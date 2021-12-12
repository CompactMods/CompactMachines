package dev.compactmods.machines.network;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.tunnel.client.ClientTunnelHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class TunnelAddedPacket {

    @Nonnull
    private final BlockPos position;

    @Nonnull
    private final TunnelDefinition type;

    public TunnelAddedPacket(@NotNull BlockPos tunnelPos, @NotNull TunnelDefinition tunnelType) {
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
        buf.writeResourceLocation(Objects.requireNonNull(pkt.type.getRegistryName()));
    }
}
