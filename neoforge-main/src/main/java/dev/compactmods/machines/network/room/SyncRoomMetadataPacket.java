package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.room.ClientRoomPacketHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.UUID;

public record SyncRoomMetadataPacket(String roomCode, UUID owner) implements CustomPacketPayload {

  public static final Type<SyncRoomMetadataPacket> TYPE = new Type<>(CompactMachines.modRL("sync_room_metadata"));

  public static final StreamCodec<FriendlyByteBuf, SyncRoomMetadataPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, SyncRoomMetadataPacket::roomCode,
		UUIDUtil.STREAM_CODEC, SyncRoomMetadataPacket::owner,
		SyncRoomMetadataPacket::new
  );

  public static final IPayloadHandler<SyncRoomMetadataPacket> HANDLER = (pkt, ctx) -> {
	 ClientRoomPacketHandler.handleRoomSync(pkt.roomCode);
  };

  @Override
  public Type<? extends CustomPacketPayload> type() {
	 return TYPE;
  }
}
