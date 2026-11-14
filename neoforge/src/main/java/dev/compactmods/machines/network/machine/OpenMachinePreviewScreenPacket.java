package dev.compactmods.machines.network.machine;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.client.machine.ClientMachinePacketHandler;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record OpenMachinePreviewScreenPacket(GlobalPos machinePos, String roomCode) implements CustomPacketPayload {

    public static final Type<OpenMachinePreviewScreenPacket> TYPE = new Type<>(CompactMachinesCore.identifier("open_machine_preview_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenMachinePreviewScreenPacket> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, OpenMachinePreviewScreenPacket::machinePos,
            ByteBufCodecs.STRING_UTF8, OpenMachinePreviewScreenPacket::roomCode,
            OpenMachinePreviewScreenPacket::new
    );

    public static final IPayloadHandler<OpenMachinePreviewScreenPacket> HANDLER = (pkt, ctx) -> {
        ClientMachinePacketHandler.openRoomPreviewScreen(pkt);
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

