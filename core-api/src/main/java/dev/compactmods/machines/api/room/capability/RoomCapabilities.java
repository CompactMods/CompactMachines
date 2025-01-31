package dev.compactmods.machines.api.room.capability;

import dev.compactmods.machines.api.CompactMachines;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.UUID;

public interface RoomCapabilities {

    CompactRoomCapability<IAttachmentHolder, Void> ROOM_DATA_ATTACHMENTS = CompactRoomCapability.createVoid(CompactMachines.modRL("data_attachments"), IAttachmentHolder.class);

    CompactRoomCapability<IAttachmentHolder, UUID> UPGRADE_DATA_ATTACHMENTS = CompactRoomCapability.create(CompactMachines.modRL("upgrade_data_attachments"), IAttachmentHolder.class, UUID.class);
}
