package dev.compactmods.machines.neoforge.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.room.ui.upgrades.RoomUpgradeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;

public record PlayerRequestedUpgradeMenuPacket(String roomCode) implements CustomPacketPayload {
    public static final Codec<PlayerRequestedUpgradeMenuPacket> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(PlayerRequestedUpgradeMenuPacket::roomCode)
    ).apply(inst, PlayerRequestedUpgradeMenuPacket::new));

    public static final ResourceLocation ID = CompactMachines.rl("player_wants_to_open_room_upgrade_menu");
    public static final IPlayPayloadHandler<PlayerRequestedUpgradeMenuPacket> HANDLER = (pkt, ctx) -> {
        ctx.player().ifPresent(player -> {
            RoomApi.room(pkt.roomCode()).ifPresent(inst -> {
                player.openMenu(RoomUpgradeMenu.provider(inst), buf -> {
                    buf.writeUtf(pkt.roomCode());
                });
            });
        });
    };

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeJsonWithCodec(CODEC, this);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
