package dev.compactmods.machines.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.IRoomLookup;
import dev.compactmods.machines.api.room.registration.IMutableRoomRegistration;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.upgrade.ILevelLoadedUpgradeListener;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;

public class ChunkloadUpgrade implements RoomUpgrade, ILevelLoadedUpgradeListener {

    public static final ResourceLocation REG_ID = new ResourceLocation(Constants.MOD_ID, "chunkloader");
    private static final TicketType<ChunkPos> CM4_LOAD_TYPE = TicketType.create(Constants.MOD_ID + ":rooms", Comparator.comparingLong(ChunkPos::toLong));

    private static final Codec<ChunkloadUpgrade> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> REG_ID)
    ).apply(i, t -> new ChunkloadUpgrade()));

    @Override
    public String getTranslationKey() {
        return "item." + REG_ID.getNamespace() + ".upgrades." + REG_ID.getPath();
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

    private void forceLoad(ServerLevel level, IRoomRegistration room) {
        final var chunks = level.getChunkSource();
        room.chunks().forEach(chunk -> {
            level.setChunkForced(chunk.x, chunk.z, true);
            chunks.addRegionTicket(CM4_LOAD_TYPE, chunk, 2, chunk);
        });

        chunks.save(false);
    }

    private void normalLoad(ServerLevel level, IRoomRegistration room) {
        final var chunks = level.getChunkSource();
        room.chunks().forEach(chunk -> {
            level.setChunkForced(chunk.x, chunk.z, false);
            chunks.removeRegionTicket(CM4_LOAD_TYPE, chunk, 2, chunk);
        });

        chunks.save(false);
    }
}

