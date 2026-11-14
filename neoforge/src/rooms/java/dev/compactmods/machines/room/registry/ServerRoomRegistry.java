package dev.compactmods.machines.room.registry;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.data.manager.CodecFileManager;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import dev.compactmods.machines.room.generation.RoomCodeGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class ServerRoomRegistry implements RoomRegistry, AutoCloseable {

    private final MinecraftServer server;
    private final CodecFileManager.Keyed<String, RoomInstanceData> ROOM_REGISTRAR_DATA;
    private final Map<String, RoomInstance> instanceCache;

    public ServerRoomRegistry(MinecraftServer server) {
        this.server = server;
        this.instanceCache = new Object2ObjectArrayMap<>();

        ROOM_REGISTRAR_DATA = CodecFileManager.keyed(Function.identity(), RoomInstanceData.CODEC)
                .at(CMRoomDataLocations.REGISTRY_FILES)
                .build(server);
    }

    @Override
    public boolean isRegistered(String room) {
        return ROOM_REGISTRAR_DATA.hasData(room);
    }

    @Override
    public Optional<RoomInstance> register(Holder<RoomTemplate> template, RoomBoundaries boundaries, UUID owner) {
        final var newCode = RoomCodeGenerator.generateRoomId();
        var serverData = new RoomInstanceData(newCode, boundaries);
        ROOM_REGISTRAR_DATA.set(newCode, serverData);
        ROOM_REGISTRAR_DATA.save();

        var instance = new ServerRoomInstance(server, CompactDimension.LEVEL_KEY, newCode, boundaries);
        return Optional.of(instance);
    }

    @Override
    public Optional<RoomInstance> get(String room) {
        return ROOM_REGISTRAR_DATA.optionalData(room)
                .map(this::getOrMakeRoomInstance);
    }

    @Override
    public int count() {
        return Math.toIntExact(ROOM_REGISTRAR_DATA.existingFiles().count());
    }

    @Override
    public Stream<String> allRoomCodes() {
        // TODO: Filter via room code regex rather than length here
        return ROOM_REGISTRAR_DATA.existingFiles()
                .filter(f -> f.length() == 14);
    }

    @Override
    public Stream<RoomInstance> allRooms() {
        return ROOM_REGISTRAR_DATA.existingFiles()
                .map(ROOM_REGISTRAR_DATA::data)
                .filter(Objects::nonNull)
                .map(this::getOrMakeRoomInstance);
    }

    @Override
    public void save() {
        ROOM_REGISTRAR_DATA.save();
    }

    @NotNull
    private RoomInstance getOrMakeRoomInstance(RoomInstanceData instanceData) {
        if (instanceCache.containsKey(instanceData.roomCode()))
            return instanceCache.get(instanceData.roomCode());

        final var inst = new ServerRoomInstance(server, CompactDimension.LEVEL_KEY, instanceData.roomCode(), instanceData.boundaries());
        instanceCache.put(instanceData.roomCode(), inst);
        return inst;
    }

    @Override
    public void close() {
        save();
    }
}
