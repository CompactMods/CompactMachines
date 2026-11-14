package dev.compactmods.machines.upgrades.storage.tunnel.preview;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.room.preview.server.RoomPreviewContributor;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.compactmods.machines.upgrades.storage.tunnel.RoomStorageLinks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Optional;

/** Sends the room preview a room's tunnels: where they are, their storages, and the machines exposing them. */
public final class TunnelPreviewContributor implements RoomPreviewContributor {

    public static final TunnelPreviewContributor INSTANCE = new TunnelPreviewContributor();

    private TunnelPreviewContributor() {}

    @Override
    public Optional<CustomPacketPayload> capture(ServerLevel level, RoomInstance room) {
        final var storages = room.getCapability(StorageCapabilities.NAMED_STORAGES);
        if (storages == null)
            return Optional.empty();

        final var links = RoomStorageLinks.prune(level.getServer(), room);

        // Same origin as RoomPreviewSnapshot#capture: the interior's minimum block corner
        final AABB inner = room.boundaries().innerBounds();
        final BlockPos origin = new BlockPos(Mth.floor(inner.minX), Mth.floor(inner.minY), Mth.floor(inner.minZ));
        final BlockPos max = new BlockPos(Mth.ceil(inner.maxX), Mth.ceil(inner.maxY), Mth.ceil(inner.maxZ));

        final var markers = new ArrayList<TunnelMarker>();
        for (var def : storages.definitions().storages().values()) {
            if (def.tunnel().isEmpty())
                continue;

            final BlockPos tunnel = def.tunnel().get();
            final var outward = outward(tunnel, origin, max);
            final var type = storages.tunnelType(def.type());
            if (outward.isEmpty() || type.isEmpty())
                continue;

            markers.add(new TunnelMarker(tunnel.subtract(origin), outward.get(), type.get().value().color(),
                    def.name(), def.type(), links.linksTo(def.name())));
        }

        return Optional.of(new RoomTunnelsPacket(room.code(), markers));
    }

    /** {@return which side of the interior {@code [min, max)} the wall at {@code pos} is on} */
    static Optional<Direction> outward(BlockPos pos, BlockPos min, BlockPos max) {
        if (pos.getX() < min.getX()) return Optional.of(Direction.WEST);
        if (pos.getX() >= max.getX()) return Optional.of(Direction.EAST);
        if (pos.getY() < min.getY()) return Optional.of(Direction.DOWN);
        if (pos.getY() >= max.getY()) return Optional.of(Direction.UP);
        if (pos.getZ() < min.getZ()) return Optional.of(Direction.NORTH);
        if (pos.getZ() >= max.getZ()) return Optional.of(Direction.SOUTH);
        return Optional.empty();
    }
}
