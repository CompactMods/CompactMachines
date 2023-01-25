package dev.compactmods.machines.tunnel.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.tunnel.client.ClientTunnelHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TunnelAddedPacket(BlockPos position, ResourceLocation type, GlobalPos machine) {

    public static final Codec<TunnelAddedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("position").forGetter(TunnelAddedPacket::position),
            ResourceLocation.CODEC.fieldOf("definition").forGetter(TunnelAddedPacket::type),
            GlobalPos.CODEC.fieldOf("machine").forGetter(TunnelAddedPacket::machine)
    ).apply(i, TunnelAddedPacket::new));

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        final var tunnelType = Tunnels.getDefinition(type);
        ctx.enqueueWork(() -> {
            ClientTunnelHandler.setTunnel(position, tunnelType);
        });

        ctx.setPacketHandled(true);
    }
}
