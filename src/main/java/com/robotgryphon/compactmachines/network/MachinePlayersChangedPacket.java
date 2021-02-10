package com.robotgryphon.compactmachines.network;

import com.google.common.collect.ImmutableSet;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.client.machine.MachinePlayerEventHandler;
import com.robotgryphon.compactmachines.data.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.SavedMachineData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class MachinePlayersChangedPacket {

    private MinecraftServer server;
    public int machineID;
    public ImmutableSet<DimensionalPosition> machinePositions;
    public EnumPlayerChangeType type;
    public UUID playerID;

    public MachinePlayersChangedPacket(@Nullable MinecraftServer server, int machineID, UUID id, EnumPlayerChangeType type) {
        this.server = server;
        this.machineID = machineID;
        this.machinePositions = ImmutableSet.of();
        this.playerID = id;
        this.type = type;
    }

    public static void handle(MachinePlayersChangedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        message.machinePositions.forEach(machinePos -> {
            CompactMachines.LOGGER.debug("Player changed machine {}; outer position {}", message.machineID, machinePos);
            MachinePlayerEventHandler.handlePlayerMachineChanged(message.playerID, message.type, machinePos);
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(MachinePlayersChangedPacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.machineID);
        buf.writeUniqueId(pkt.playerID);
        buf.writeString(pkt.type.toString());

        SavedMachineData md = SavedMachineData.getInstance(pkt.server);
        CompactMachineServerData data = md.getData();

        Optional<CompactMachineRegistrationData> machineData = data.getMachineData(pkt.machineID);
        buf.writeBoolean(machineData.isPresent());
        machineData.ifPresent(mData -> {
            DimensionalPosition out = mData.getOutsidePosition(pkt.server);
            buf.writeCompoundTag(out.serializeNBT());
        });
    }

    public static MachinePlayersChangedPacket decode(PacketBuffer buf) {
        int machine = buf.readInt();
        UUID id = buf.readUniqueId();
        EnumPlayerChangeType changeType = EnumPlayerChangeType.valueOf(buf.readString());

        MachinePlayersChangedPacket pkt = new MachinePlayersChangedPacket(null, machine, id, changeType);
        if(buf.readBoolean()) {
            DimensionalPosition tilePos = DimensionalPosition.fromNBT(buf.readCompoundTag());
            pkt.machinePositions = ImmutableSet.of(tilePos);
        }

        return pkt;
    }

    public enum EnumPlayerChangeType {
        ENTERED,
        EXITED
    }
}
