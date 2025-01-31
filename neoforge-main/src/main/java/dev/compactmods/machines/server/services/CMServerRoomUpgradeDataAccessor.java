package dev.compactmods.machines.server.services;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstanceKey;
import dev.compactmods.machines.api.room.upgrade.data.IRoomUpgradeDataAttachmentAccessor;
import dev.compactmods.machines.data.manager.CMKeyedDataFileManager;
import dev.compactmods.machines.room.upgrade.RoomUpgradeDataAttachments;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;
import java.util.UUID;

public class CMServerRoomUpgradeDataAccessor implements IRoomUpgradeDataAttachmentAccessor {
    private final CMKeyedDataFileManager<RoomUpgradeInstanceKey, RoomUpgradeDataAttachments> DATA_ATTACHMENTS;

    public CMServerRoomUpgradeDataAccessor() {
        DATA_ATTACHMENTS = new CMKeyedDataFileManager<>(CompactMachinesServer.currentServer(), RoomUpgradeDataAttachments::new) {
            @Override
            public String getFileKey(RoomUpgradeInstanceKey key) {
                return key.instanceId().toString();
            }
        };
    }

    @Override
    public Optional<? extends IAttachmentHolder> get(String roomCode, UUID instanceId) {
        return DATA_ATTACHMENTS.optionalData(new RoomUpgradeInstanceKey(roomCode, instanceId));
    }

    @Override
    public IAttachmentHolder getOrCreate(String roomCode, UUID instanceId) {
        return DATA_ATTACHMENTS.data(new RoomUpgradeInstanceKey(roomCode, instanceId));
    }

    @Override
    public void save() {
        DATA_ATTACHMENTS.save();
    }
}
