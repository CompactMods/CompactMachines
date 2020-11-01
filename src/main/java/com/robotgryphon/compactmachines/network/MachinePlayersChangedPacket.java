package com.robotgryphon.compactmachines.network;

import com.robotgryphon.compactmachines.data.CompactMachineClientData;
import com.robotgryphon.compactmachines.data.CompactMachineCommonData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class MachinePlayersChangedPacket {

    private int machineID;
    private EnumPlayerChangeType type;
    private UUID playerID;

    public MachinePlayersChangedPacket(int machineID, UUID id, EnumPlayerChangeType type) {
        this.machineID = machineID;
        this.playerID = id;
        this.type = type;
    }

    public static void handle(MachinePlayersChangedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        CompactMachineCommonData instance = CompactMachineClientData.getInstance();
        Optional<CompactMachinePlayerData> playerData = instance.getPlayerData(message.machineID);
        playerData.ifPresent(pd -> {
            switch(message.type) {
                case EXITED:
                    pd.removePlayer(message.playerID);
                    break;

                case ENTERED:
                    pd.addPlayer(message.playerID);
                    break;
            }

            instance.updatePlayerData(pd);
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(MachinePlayersChangedPacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.machineID);
        buf.writeUniqueId(pkt.playerID);
        buf.writeString(pkt.type.toString());
    }

    public static MachinePlayersChangedPacket decode(PacketBuffer buf) {
        int machine = buf.readInt();
        UUID id = buf.readUniqueId();
        EnumPlayerChangeType changeType = EnumPlayerChangeType.valueOf(buf.readString());

        return new MachinePlayersChangedPacket(machine, id, changeType);
    }

    public enum EnumPlayerChangeType {
        ENTERED,
        EXITED
    }
}
