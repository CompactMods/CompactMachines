package dev.compactmods.machines.upgrade;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.upgrade.ILevelLoadedUpgradeListener;
import dev.compactmods.machines.api.upgrade.IUpgradeAppliedListener;
import dev.compactmods.machines.api.upgrade.IUpgradeRemovedListener;
import dev.compactmods.machines.api.upgrade.RoomUpgradeAction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;

public class ChunkloadAction implements RoomUpgradeAction,
        IUpgradeAppliedListener, IUpgradeRemovedListener,
        ILevelLoadedUpgradeListener {

    private static final TicketType<ChunkPos> CHUNKLOAD_LOAD_TYPE = TicketType.create(Constants.MOD_ID + ":rooms", Comparator.comparingLong(ChunkPos::toLong));

    private static final Codec<ChunkloadAction> CODEC = Codec.unit(new ChunkloadAction());

    @Override
    public Codec<? extends RoomUpgradeAction> codec() {
        return CODEC;
    }

    @Override
    public void onAdded(ServerLevel level, IRoomRegistration room) {
        forceLoad(level, room);
    }

    @Override
    public void onRemoved(ServerLevel level, IRoomRegistration room) {
        normalLoad(level, room);
    }

    @Override
    public void onLevelLoaded(ServerLevel level, IRoomRegistration room) {
        forceLoad(level, room);
    }

    @Override
    public void onLevelUnloaded(ServerLevel level, IRoomRegistration room) {
        normalLoad(level, room);
    }

    private static void forceLoad(ServerLevel level, IRoomRegistration room) {
        final var chunks = level.getChunkSource();
        room.chunks().forEach(chunk -> {
            level.setChunkForced(chunk.x, chunk.z, true);
            chunks.addRegionTicket(CHUNKLOAD_LOAD_TYPE, chunk, 2, chunk);
        });

        chunks.save(false);
    }

    private static void normalLoad(ServerLevel level, IRoomRegistration room) {
        final var chunks = level.getChunkSource();
        room.chunks().forEach(chunk -> {
            level.setChunkForced(chunk.x, chunk.z, false);
            chunks.removeRegionTicket(CHUNKLOAD_LOAD_TYPE, chunk, 2, chunk);
        });

        chunks.save(false);
    }
}

