package dev.compactmods.machines.upgrades.api.data;

import dev.compactmods.machines.core.data.Saveable;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;
import java.util.UUID;

public interface IRoomUpgradeDataAttachmentAccessor extends Saveable {
   Optional<? extends IAttachmentHolder> get(String roomCode, UUID instanceId);

   IAttachmentHolder getOrCreate(String roomCode, UUID instanceId);

   void save();
}
