package dev.compactmods.machines.room.attachment;

import dev.compactmods.machines.core.attachment.AttachmentDataAccessor;
import dev.compactmods.machines.core.attachment.CMAttachmentHolder;
import dev.compactmods.machines.core.attachment.storage.AttachmentDataStorage;
import dev.compactmods.machines.core.attachment.persistence.AttachmentIOHelper;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import net.minecraft.server.MinecraftServer;

public class RoomDataAttachments implements AutoCloseable, AttachmentDataAccessor {

    private final MinecraftServer server;
    private final String roomCode;
    private final CMAttachmentHolder attachments;

    public RoomDataAttachments(MinecraftServer server, String roomCode) {
        this.server = server;
        this.roomCode = roomCode;
        this.attachments = new CMAttachmentHolder();

        final var file = CMRoomDataLocations.ROOM_DATA_ATTACHMENTS
                .apply(server)
                .resolve(roomCode + ".dat");

        AttachmentIOHelper.load(server.registryAccess(), attachments, file);
    }

    @Override
    public AttachmentDataStorage dataStorage() {
        return attachments.dataStorage();
    }

    public void save() {
        final var file = CMRoomDataLocations.ROOM_DATA_ATTACHMENTS
                .apply(server)
                .resolve(roomCode + ".dat");

        AttachmentIOHelper.save(server.registryAccess(), attachments, file);
    }

    @Override
    public void close() {
        save();
    }
}
