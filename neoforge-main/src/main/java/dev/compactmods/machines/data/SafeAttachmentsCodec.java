package dev.compactmods.machines.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentHolder;

public class SafeAttachmentsCodec {

    public static CompoundTag safeSerialize(AttachmentHolder inst, MinecraftServer server) {
        try {
            final var s = inst.serializeAttachments(server.registryAccess());
            return s == null ? new CompoundTag() : s;
        }

        catch(Exception e) {
            return new CompoundTag();
        }
    }
}
