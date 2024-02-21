package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.room.RoomHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record PlayerRequestedLeavePacket() implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "player_requested_to_leave_room");

    public static final IPlayPayloadHandler<PlayerRequestedLeavePacket> HANDLER = (pkt, ctx) -> {
        ctx.player().ifPresent(p -> {
            if(p instanceof ServerPlayer sp) {
                RoomHelper.teleportPlayerOutOfRoom(sp);
            }
        });
    };

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {

    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
