package dev.compactmods.machines.neoforge.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.room.ui.preview.MachineRoomMenu;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.extensions.IServerGamePacketListenerExtension;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;

import java.util.Optional;

public record PlayerRequestedRoomUIPacket(String roomCode) implements CustomPacketPayload {
    public static final Codec<PlayerRequestedRoomUIPacket> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(PlayerRequestedRoomUIPacket::roomCode)
    ).apply(inst, PlayerRequestedRoomUIPacket::new));

    public static final ResourceLocation ID = CompactMachines.rl("player_wants_to_open_room_ui");
    public static final IPlayPayloadHandler<PlayerRequestedRoomUIPacket> HANDLER = (pkt, ctx) -> {
        ctx.player().ifPresent(player -> {
            RoomApi.room(pkt.roomCode).ifPresent(inst -> {
                final var server = player.getServer();
                final var pos = player.getData(Rooms.OPEN_MACHINE_POS);
                player.openMenu(MachineRoomMenu.provider(server, inst), buf -> {
                    buf.writeJsonWithCodec(GlobalPos.CODEC, pos);
                    buf.writeUtf(pkt.roomCode);
                    buf.writeOptional(Optional.<String>empty(), FriendlyByteBuf::writeUtf);
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
