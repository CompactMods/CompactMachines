package dev.compactmods.machines.upgrades.storage.tunnel.menu;

import dev.compactmods.machines.upgrades.storage.tunnel.RoomStorageDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * What the tunnel config screen shows in edit mode: the bound storage and its current capacity.
 *
 * @param storage  The storage the tunnel is bound to.
 * @param capacity The storage's capacity: slots for items, buckets for fluids.
 */
public record TunnelConfigState(RoomStorageDefinition storage, int capacity) {

    public static final StreamCodec<RegistryFriendlyByteBuf, TunnelConfigState> STREAM_CODEC = StreamCodec.composite(
            RoomStorageDefinition.STREAM_CODEC, TunnelConfigState::storage,
            ByteBufCodecs.VAR_INT, TunnelConfigState::capacity,
            TunnelConfigState::new);
}
