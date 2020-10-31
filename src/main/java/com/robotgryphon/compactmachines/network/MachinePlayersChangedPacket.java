package com.robotgryphon.compactmachines.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MachinePlayersChangedPacket {

    private UUID playerID;

    public MachinePlayersChangedPacket(UUID id) {
        this.playerID = id;
    }

    public static void handle(MachinePlayersChangedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        
        ctx.setPacketHandled(true);
    }

    public static void encode(MachinePlayersChangedPacket pkt, PacketBuffer buf) {
        buf.writeUniqueId(pkt.playerID);
    }

    public static MachinePlayersChangedPacket decode(PacketBuffer buf) {
        UUID id = buf.readUniqueId();
        return new MachinePlayersChangedPacket(id);
    }
}
