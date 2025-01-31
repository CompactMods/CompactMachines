package dev.compactmods.machines.api.room.data;

import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.Optional;

public interface IRoomDataAttachmentAccessor {
   Optional<? extends IAttachmentHolder> get(String roomCode);

   IAttachmentHolder getOrCreate(String roomCode);

   void save();
}
