package dev.compactmods.machines.room.data;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.function.Function;

public interface CMRoomDataLocations {

    Function<MinecraftServer, Path> DATA_ROOT = (server) -> server
            .getServerDirectory()
            .resolve(server.getWorldPath(LevelResource.DATA))
            .resolve(CompactMachinesCore.MOD_ID);

    Function<MinecraftServer, Path> REGISTRY_FILES = (server) -> DATA_ROOT.apply(server)
            .resolve("registry");

    Function<MinecraftServer, Path> PLAYER_SPAWNS = (server) -> DATA_ROOT.apply(server)
            .resolve("player_spawns");

    Function<MinecraftServer, Path> ROOM_DATA_ATTACHMENTS = (server) -> DATA_ROOT.apply(server)
            .resolve("room_data");

}
