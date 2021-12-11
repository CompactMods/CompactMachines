package dev.compactmods.machines.network;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class TunnelAddedPacket {

    private final BlockPos position;
    private final ResourceLocation type;

    public TunnelAddedPacket(BlockPos tunnelPos, ResourceLocation tunnelType) {
        this.position = tunnelPos;
        this.type = tunnelType;
    }

    public TunnelAddedPacket(FriendlyByteBuf buf) {
        position = buf.readBlockPos();
        type = buf.readResourceLocation();
    }

    public static void handle(TunnelAddedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

//        ctx.enqueueWork(() -> {
//            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
//                ClientTunnelHandler.updateTunnelData(message.position, message.type);
//                return null;
//            });
//        });

        ctx.setPacketHandled(true);
    }

    public static void encode(TunnelAddedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.position);
        buf.writeResourceLocation(pkt.type);
    }
}
