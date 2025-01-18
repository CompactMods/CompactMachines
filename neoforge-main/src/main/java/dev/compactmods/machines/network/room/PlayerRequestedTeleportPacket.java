package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.room.RoomHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record PlayerRequestedTeleportPacket(GlobalPos machine, String room) implements CustomPacketPayload {

  public static final Type<PlayerRequestedTeleportPacket> TYPE = new Type<>(CompactMachines.modRL("player_teleport"));

  public static final StreamCodec<FriendlyByteBuf, PlayerRequestedTeleportPacket> STREAM_CODEC = StreamCodec.composite(
		GlobalPos.STREAM_CODEC, PlayerRequestedTeleportPacket::machine,
		ByteBufCodecs.STRING_UTF8, PlayerRequestedTeleportPacket::room,
		PlayerRequestedTeleportPacket::new
  );

  public static final IPayloadHandler<PlayerRequestedTeleportPacket> HANDLER = (pkt, ctx) -> {
	 ctx.enqueueWork(() -> {
		final var player = ctx.player();
		if (player instanceof ServerPlayer sp) {
		  RoomHelper.teleportPlayerIntoMachine(player.level(), sp, pkt.machine, pkt.room);
		}
	 });
  };

  @Override
  public Type<? extends CustomPacketPayload> type() {
	 return TYPE;
  }
}
