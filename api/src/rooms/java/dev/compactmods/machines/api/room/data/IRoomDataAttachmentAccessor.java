package dev.compactmods.machines.api.room.data;

import dev.compactmods.machines.core.data.Saveable;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;

public interface IRoomDataAttachmentAccessor extends Saveable {
   Optional<? extends IAttachmentHolder> get(String roomCode);

   IAttachmentHolder getOrCreate(String roomCode);
}
