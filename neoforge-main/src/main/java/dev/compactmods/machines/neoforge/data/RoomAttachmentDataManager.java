package dev.compactmods.machines.neoforge.data;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public class RoomAttachmentDataManager {

    private static RoomAttachmentDataManager INSTANCE;
    private final MinecraftServer server;
    private final HashMap<String, RoomAttachmentData> cache;

    private RoomAttachmentDataManager(MinecraftServer server) {
        this.server = server;
        this.cache = new HashMap<>();
    }

    public static Optional<RoomAttachmentDataManager> instance() {
        return Optional.ofNullable(INSTANCE);
    }

    public static RoomAttachmentDataManager instance(MinecraftServer server) {
        if(INSTANCE == null) {
            INSTANCE = new RoomAttachmentDataManager(server);
            return INSTANCE;
        }

        // Compare instance servers. If they aren't a match, save the old instance and set up fresh
        if(INSTANCE.server != server) {
            INSTANCE.save();
            INSTANCE = new RoomAttachmentDataManager(server);
            return INSTANCE;
        } else {
            return INSTANCE;
        }
    }

    public RoomAttachmentData data(String roomCode) {
        return cache.computeIfAbsent(roomCode, k -> RoomAttachmentData.createForRoom(server, roomCode));
    }

    public void save() {
        cache.forEach((key, data) -> data.save());
    }
}
