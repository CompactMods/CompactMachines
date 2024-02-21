package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.room.RoomHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record PlayerRequestedTeleportPacket(GlobalPos machine, String room) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "player_teleport");

    public static final FriendlyByteBuf.Reader<PlayerRequestedTeleportPacket> READER = (buf) -> {
        final var gp = buf.readJsonWithCodec(GlobalPos.CODEC);
        final var roomCode = buf.readUtf();
        return new PlayerRequestedTeleportPacket(gp, roomCode);
    };
    public static final IPlayPayloadHandler<PlayerRequestedTeleportPacket> HANDLER = (pkt, ctx) -> {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                if(player instanceof ServerPlayer sp) {
                    RoomHelper.teleportPlayerIntoMachine(player.level(), sp, pkt.machine, pkt.room);
                }
            });
        });
    };

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(GlobalPos.CODEC, machine);
        buf.writeUtf(room);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
