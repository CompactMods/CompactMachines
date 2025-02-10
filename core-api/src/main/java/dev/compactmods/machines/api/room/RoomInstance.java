package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.attachment.IForwardingAttachmentHolder;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.room.capability.CompactRoomCapability;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.spatial.IRoomBoundaries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record RoomInstance(MinecraftServer server, ResourceKey<Level> levelKey, String code, MachineColor defaultMachineColor, IRoomBoundaries boundaries) implements IForwardingAttachmentHolder {

    public ServerLevel level() {
        return server.getLevel(levelKey);
    }

    public <T, C> T getCapability(CompactRoomCapability<T, C> capability) {
        return capability.getCapability(server, code, null);
    }

    public <T, C> T getCapability(CompactRoomCapability<T, C> capability, @Nullable C context) {
        return capability.getCapability(server, code, context);
    }

    @Override
    public Supplier<IAttachmentHolder> attachmentHolder() {
        return () -> getCapability(RoomCapabilities.ROOM_DATA_ATTACHMENTS);
    }
}
