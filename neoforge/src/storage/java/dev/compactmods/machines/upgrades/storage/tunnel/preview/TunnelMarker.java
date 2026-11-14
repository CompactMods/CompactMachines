package dev.compactmods.machines.upgrades.storage.tunnel.preview;

import dev.compactmods.machines.upgrades.storage.tunnel.RoomStorageLinks;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

import java.util.List;

/**
 * A tunnel as the room preview shows it.
 *
 * @param local   The tunnel wall's position, room-local (relative to the interior's minimum corner, so
 *                walls sit at {@code -1} and {@code size}).
 * @param outward The wall's outward face, away from the room.
 * @param color   The tunnel type's color, packed {@code 0xRRGGBB}.
 * @param name    The named storage it's bound to.
 * @param type    The storage's tunnel type.
 * @param links   The machines exposing the storage.
 */
public record TunnelMarker(BlockPos local, Direction outward, int color, String name,
                           ResourceKey<TunnelType> type, List<RoomStorageLinks.Link> links) {

    public static final StreamCodec<ByteBuf, TunnelMarker> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TunnelMarker::local,
            Direction.STREAM_CODEC, TunnelMarker::outward,
            ByteBufCodecs.INT, TunnelMarker::color,
            ByteBufCodecs.STRING_UTF8, TunnelMarker::name,
            ResourceKey.streamCodec(TunnelType.REGISTRY_KEY), TunnelMarker::type,
            RoomStorageLinks.Link.STREAM_CODEC.apply(ByteBufCodecs.list()), TunnelMarker::links,
            TunnelMarker::new);

    public TunnelMarker {
        links = List.copyOf(links);
    }
}
