package dev.compactmods.machines.core.attachment.storage;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Objects;

public interface ValidatingAttachmentDataStorage {
    boolean IN_DEV = !FMLEnvironment.isProduction();

    default void validateAttachmentType(AttachmentType<?> type) {
        Objects.requireNonNull(type);
        if (!IN_DEV) return;

        if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Data attachment type [" + type.getClass().getName() + "] must be registered!");
        }
    }
}
