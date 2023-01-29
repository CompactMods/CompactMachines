package dev.compactmods.machines.tunnel.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.tunnel.client.ClientTunnelHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record MachineTunnelsUpdatePacket(BlockPos machine, List<GlobalPos> tunnels) {

    public static final Codec<MachineTunnelsUpdatePacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("machine").forGetter(MachineTunnelsUpdatePacket::machine),
            GlobalPos.CODEC.listOf().fieldOf("tunnels").forGetter(MachineTunnelsUpdatePacket::tunnels)
    ).apply(i, MachineTunnelsUpdatePacket::new));

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        final var context = contextSupplier.get();
        context.enqueueWork(() -> ClientTunnelHandler.updateMachineTunnels(machine, tunnels));
        return true;
    }
}
