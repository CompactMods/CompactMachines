package dev.compactmods.machines.room.registry;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.core.attachment.IForwardingAttachmentHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record ServerRoomInstance(
        MinecraftServer server, ResourceKey<Level> levelKey,
        String code, RoomBoundaries boundaries
) implements RoomInstance, IForwardingAttachmentHolder {

    public ServerLevel level() {
        return server.getLevel(levelKey);
    }

    public <T, C> T getCapability(RoomCapability<T, C> capability) {
        return capability.getCapability(server, this, null);
    }

    public <T, C> T getCapability(RoomCapability<T, C> capability, @Nullable C context) {
        return capability.getCapability(server, this, context);
    }

    @Override
    public Supplier<IAttachmentHolder> getAttachmentHolder() {
        return () -> getCapability(RoomCapabilities.ROOM_DATA_ATTACHMENTS);
    }
}