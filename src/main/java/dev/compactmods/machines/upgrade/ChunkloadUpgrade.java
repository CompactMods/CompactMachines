package dev.compactmods.machines.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.ILevelLoadedUpgradeListener;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Comparator;

public class ChunkloadUpgrade extends ForgeRegistryEntry<RoomUpgrade> implements RoomUpgrade, ILevelLoadedUpgradeListener {

    public static final ResourceLocation REG_ID = new ResourceLocation(CompactMachines.MOD_ID, "chunkloader");
    private static final TicketType<ChunkPos> CM4_LOAD_TYPE = TicketType.create(CompactMachines.MOD_ID + ":rooms", Comparator.comparingLong(ChunkPos::toLong));

    private static final Codec<ChunkloadUpgrade> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> REG_ID)
    ).apply(i, t -> new ChunkloadUpgrade()));

    @Override
    public void onAdded(ServerLevel level, ChunkPos room) { forceLoad(level, room); }

    @Override
    public void onRemoved(ServerLevel level, ChunkPos room) { normalLoad(level, room); }

    @Override
    public void onLevelLoaded(ServerLevel level, ChunkPos room) { forceLoad(level, room); }

    @Override
    public void onLevelUnloaded(ServerLevel level, ChunkPos room) { normalLoad(level, room); }

    private void forceLoad(ServerLevel level, ChunkPos room) {
        final var chunks = level.getChunkSource();
        level.setChunkForced(room.x, room.z, true);
        chunks.registerTickingTicket(CM4_LOAD_TYPE, room, 2, room);
        chunks.save(false);
    }

    private void normalLoad(ServerLevel level, ChunkPos room) {
        final var chunks = level.getChunkSource();
        level.setChunkForced(room.x, room.z, false);
        chunks.releaseTickingTicket(CM4_LOAD_TYPE, room, 2, room);
        chunks.save(false);
    }
}

