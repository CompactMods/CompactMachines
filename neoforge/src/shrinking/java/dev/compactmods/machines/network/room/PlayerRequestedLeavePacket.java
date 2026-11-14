package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record PlayerRequestedLeavePacket() implements CustomPacketPayload {

    public static final Type<PlayerRequestedLeavePacket> TYPE = new Type<>(CompactMachinesCore.identifier("player_requested_to_leave_room"));

    public static final IPayloadHandler<PlayerRequestedLeavePacket> HANDLER = (pkt, ctx) -> {
        final var player = ctx.player();
        final var server = player.level().getServer();

        if (player instanceof ServerPlayer sp) {
            final var reg = RoomCapabilities.REGISTRY.getCapability(server);
            sp.getExistingData(Shrinking.CURRENT_ROOM_CODE)
                    .flatMap(reg::get)
                    .ifPresent(room -> {
                        var cap = sp.getCapability(Shrinking.SHRINK, room);
                        if(cap == null)
                            return;

                        cap.tryExit();
                    });

        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
