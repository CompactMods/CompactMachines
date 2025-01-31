package dev.compactmods.machines.server.services;

import dev.compactmods.machines.api.room.data.IRoomDataAttachmentAccessor;
import dev.compactmods.machines.data.manager.CMKeyedDataFileManager;
import dev.compactmods.machines.room.attachment.RoomDataAttachments;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;

@SuppressWarnings("unused")
public class CMServerRoomDataAttachmentAccessor implements IRoomDataAttachmentAccessor {

    private final CMKeyedDataFileManager<String, RoomDataAttachments> ROOM_DATA_ATTACHMENTS;

    public CMServerRoomDataAttachmentAccessor() {
        ROOM_DATA_ATTACHMENTS = new CMKeyedDataFileManager<>(CompactMachinesServer.currentServer(), RoomDataAttachments::new);
    }

    @Override
    public Optional<? extends IAttachmentHolder> get(String roomCode) {
        return ROOM_DATA_ATTACHMENTS.optionalData(roomCode);
    }

    @Override
    public IAttachmentHolder getOrCreate(String roomCode) {
        return ROOM_DATA_ATTACHMENTS.data(roomCode);
    }

    @Override
    public void save() {
        ROOM_DATA_ATTACHMENTS.save();
    }
}
