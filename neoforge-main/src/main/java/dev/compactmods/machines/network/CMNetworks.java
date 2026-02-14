package dev.compactmods.machines.network;

import dev.compactmods.machines.network.machine.MachineColorSyncPacket;
import dev.compactmods.machines.network.machine.OpenMachinePreviewScreenPacket;
import dev.compactmods.machines.network.room.InitialRoomBlockDataPacket;
import dev.compactmods.machines.network.room.PlayerRequestedLeavePacket;
import dev.compactmods.machines.network.room.PlayerRequestedRoomUIPacket;
import dev.compactmods.machines.network.room.PlayerRequestedTeleportPacket;
import dev.compactmods.machines.network.room.PlayerRequestedUpgradeUIPacket;
import dev.compactmods.machines.network.room.PlayerStartedRoomTrackingPacket;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CMNetworks {

    public static void onPacketRegistration(final RegisterPayloadHandlersEvent payloads) {
        final PayloadRegistrar main = payloads.registrar("7.2.0");

        // Machines
        main.playToClient(MachineColorSyncPacket.TYPE, MachineColorSyncPacket.STREAM_CODEC, MachineColorSyncPacket.HANDLER);
        main.playToClient(OpenMachinePreviewScreenPacket.TYPE, OpenMachinePreviewScreenPacket.STREAM_CODEC, OpenMachinePreviewScreenPacket.HANDLER);

        // Rooms
        main.playToClient(InitialRoomBlockDataPacket.TYPE, InitialRoomBlockDataPacket.STREAM_CODEC, InitialRoomBlockDataPacket.HANDLER);

        main.playToServer(PlayerStartedRoomTrackingPacket.TYPE, PlayerStartedRoomTrackingPacket.STREAM_CODEC, PlayerStartedRoomTrackingPacket.HANDLER);
        main.playToServer(PlayerRequestedTeleportPacket.TYPE, PlayerRequestedTeleportPacket.STREAM_CODEC, PlayerRequestedTeleportPacket.HANDLER);
        main.playToServer(PlayerRequestedLeavePacket.TYPE, StreamCodec.unit(new PlayerRequestedLeavePacket()), PlayerRequestedLeavePacket.HANDLER);
        main.playToServer(PlayerRequestedRoomUIPacket.TYPE, PlayerRequestedRoomUIPacket.STREAM_CODEC, PlayerRequestedRoomUIPacket.HANDLER);
        main.playToServer(PlayerRequestedUpgradeUIPacket.TYPE, PlayerRequestedUpgradeUIPacket.STREAM_CODEC, PlayerRequestedUpgradeUIPacket.HANDLER);
    }
}
