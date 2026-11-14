package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.api.room.registry.RoomRegistration;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public interface RoomInstance extends IAttachmentHolder, RoomRegistration {

    <T, C> T getCapability(RoomCapability<T, C> capability);

    <T, C> T getCapability(RoomCapability<T, C> capability, @Nullable C context);
}
