package dev.compactmods.machines.machine.network;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.MachineRoomEntry;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Client → server: the machine screen's "enter room" button. Everything is re-checked here — the
 * player must be at the machine, its core bound to a room, and the player allowed to enter (see
 * {@link MachineRoomEntry#mayEnter}) — then entry runs exactly as using a shrinking device on the
 * block does.
 */
public record MachineEnterRoomPacket(GlobalPos machine) implements CustomPacketPayload {

    public static final Type<MachineEnterRoomPacket> TYPE = new Type<>(CompactMachinesCore.identifier("machine_enter_room"));

    public static final StreamCodec<FriendlyByteBuf, MachineEnterRoomPacket> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, MachineEnterRoomPacket::machine,
            MachineEnterRoomPacket::new);

    /** Same reach as the machine menu's {@code stillValid}. */
    private static final double MAX_DISTANCE = 5;

    public static final IPayloadHandler<MachineEnterRoomPacket> HANDLER = (pkt, ctx) -> ctx.enqueueWork(() -> {
        if (!(ctx.player() instanceof ServerPlayer player))
            return;

        final var pos = pkt.machine.pos();
        if (!player.level().dimension().equals(pkt.machine.dimension()) || !player.isWithinBlockInteractionRange(pos, MAX_DISTANCE))
            return;

        if (!(player.level().getBlockEntity(pos) instanceof CompactMachineBlockEntity machine) || !MachineRoomEntry.mayEnter(player))
            return;

        final var server = player.level().getServer();
        final var rooms = server == null ? null : server.getCapability(RoomCapabilities.REGISTRY);
        if (rooms == null)
            return;

        machine.connectedRoom().flatMap(rooms::get).ifPresent(room -> {
            // Use their device if they carry one (its after-use action applies); creative/op may go without
            final ItemStack device = MachineRoomEntry.findShrinkingDevice(player).orElse(ItemStack.EMPTY);
            MachineRoomEntry.tryEnter(player, room, machine, device);
        });
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
