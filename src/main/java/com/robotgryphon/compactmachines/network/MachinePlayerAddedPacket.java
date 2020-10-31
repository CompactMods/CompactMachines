package com.robotgryphon.compactmachines.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MachinePlayerAddedPacket {

    private UUID playerID;

    public MachinePlayerAddedPacket(UUID id) {
        this.playerID = id;
    }

    public static void handle(MachinePlayerAddedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        
        ctx.setPacketHandled(true);
    }

    public static void encode(MachinePlayerAddedPacket pkt, PacketBuffer buf) {
        buf.writeUniqueId(pkt.playerID);
    }

    public static MachinePlayerAddedPacket decode(PacketBuffer buf) {
        UUID id = buf.readUniqueId();
        return new MachinePlayerAddedPacket(id);
    }
}
