package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.room.ClientRoomPacketHandler;
import dev.compactmods.machines.util.codec.VanillaCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record InitialRoomBlockDataPacket(StructureTemplate blocks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<InitialRoomBlockDataPacket> TYPE = new CustomPacketPayload.Type<>(CompactMachines.modRL("initial_room_block_data"));

    public static final StreamCodec<FriendlyByteBuf, InitialRoomBlockDataPacket> STREAM_CODEC = StreamCodec.composite(
            VanillaCodecs.STRUCTURE_TEMPLATE_STREAM_CODEC, InitialRoomBlockDataPacket::blocks,
            InitialRoomBlockDataPacket::new
    );

    public static final IPayloadHandler<InitialRoomBlockDataPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> ClientRoomPacketHandler.handleBlockData(pkt.blocks));
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
