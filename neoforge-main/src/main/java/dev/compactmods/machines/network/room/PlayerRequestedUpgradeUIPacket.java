package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.room.upgrade.inventory.RoomUpgradeInventory;
import dev.compactmods.machines.room.ui.upgrades.RoomUpgradeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record PlayerRequestedUpgradeUIPacket(String roomCode, boolean isIsolated) implements CustomPacketPayload {

   public static final Type<PlayerRequestedUpgradeUIPacket> TYPE = new Type<>(CompactMachines.modRL("player_wants_to_open_room_upgrade_menu"));

   public static final IPayloadHandler<PlayerRequestedUpgradeUIPacket> HANDLER = (pkt, ctx) -> {
	  final var player = ctx.player();
	  CompactMachines.room(pkt.roomCode()).ifPresent(inst -> {
		 player.openMenu(RoomUpgradeMenu.provider(inst), buf -> {
			buf.writeBoolean(pkt.isIsolated);
			buf.writeUtf(pkt.roomCode());

			final var inv = inst.getData(CMDataAttachments.UPGRADE_ITEMS);
			RoomUpgradeInventory.STREAM_CODEC.encode(buf, inv);
		 });
	  });
   };

   public static final StreamCodec<FriendlyByteBuf, PlayerRequestedUpgradeUIPacket> STREAM_CODEC = StreamCodec.composite(
	   ByteBufCodecs.STRING_UTF8, PlayerRequestedUpgradeUIPacket::roomCode,
	   ByteBufCodecs.BOOL, PlayerRequestedUpgradeUIPacket::isIsolated,
	   PlayerRequestedUpgradeUIPacket::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
	  return TYPE;
   }
}
