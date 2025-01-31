package dev.compactmods.machines.api.room.upgrade.data;

import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;
import java.util.UUID;

public interface IRoomUpgradeDataAttachmentAccessor {
   Optional<? extends IAttachmentHolder> get(String roomCode, UUID instanceId);

   IAttachmentHolder getOrCreate(String roomCode, UUID instanceId);

   void save();
}
