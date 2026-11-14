package dev.compactmods.machines.room.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;

import java.nio.file.Path;

public record RoomInstanceData(String roomCode, RoomBoundaries boundaries) {

    public static final Codec<RoomInstanceData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("code").forGetter(RoomInstanceData::roomCode),
            RoomBoundaries.MAP_CODEC.fieldOf("boundaries").forGetter(RoomInstanceData::boundaries)
    ).apply(i, RoomInstanceData::new));

    public static final RoomInstanceData INVALID = new RoomInstanceData("", new RoomBoundaries(AABB.INFINITE));

    private Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.DATA_ROOT.apply(server).resolve("room_registrations");
    }
}
