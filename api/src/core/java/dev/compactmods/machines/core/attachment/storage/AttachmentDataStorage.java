package dev.compactmods.machines.core.attachment.storage;

import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.ApiStatus;

import java.util.stream.Stream;

public interface AttachmentDataStorage extends AttachmentDataReader, AttachmentDataWriter {
    int size();

    Stream<AttachmentType<?>> storedTypes();

    @ApiStatus.Internal
    void putDataNoSync(AttachmentType<?> type, Object copy);
}
