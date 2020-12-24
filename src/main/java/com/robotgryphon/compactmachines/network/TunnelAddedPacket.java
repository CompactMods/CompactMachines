package com.robotgryphon.compactmachines.network;

import com.robotgryphon.compactmachines.client.ClientTunnelHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TunnelAddedPacket {

    private BlockPos position;
    private ResourceLocation type;

    private TunnelAddedPacket() {
    }

    public TunnelAddedPacket(BlockPos tunnelPos, ResourceLocation tunnelType) {
        this.position = tunnelPos;
        this.type = tunnelType;
    }

    public static void handle(TunnelAddedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
                ClientTunnelHandler.updateTunnelData(message.position, message.type);
                return null;
            });
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(TunnelAddedPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.position);
        buf.writeResourceLocation(pkt.type);
    }

    public static TunnelAddedPacket decode(PacketBuffer buf) {
        TunnelAddedPacket pkt = new TunnelAddedPacket();
        pkt.position = buf.readBlockPos();
        pkt.type = buf.readResourceLocation();

        return pkt;
    }
}
