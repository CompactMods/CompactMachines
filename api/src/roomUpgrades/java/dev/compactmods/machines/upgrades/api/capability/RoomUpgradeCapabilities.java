package dev.compactmods.machines.upgrades.api.capability;

import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.UUID;

public interface RoomUpgradeCapabilities {
    RoomCapability<IAttachmentHolder, UUID> UPGRADE_DATA_ATTACHMENTS = RoomCapability.create(CompactMachinesCore.identifier("upgrade_data_attachments"), IAttachmentHolder.class, UUID.class);

//    RoomCapability<IRoomUpgradeAccessor, Void> UPGRADES = createVoidCap("components", IRoomUpgradeAccessor.class);

    private static <T> RoomCapability<T, Void> createVoidCap(String id, Class<T> type) {
        return RoomCapability.createVoid(CompactMachinesCore.identifier(id), type);
    }
}
