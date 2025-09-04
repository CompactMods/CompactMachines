package dev.compactmods.machines.data;

import net.neoforged.neoforge.attachment.IAttachmentHolder;

public record CloseableAttachmentHolder(IAttachmentHolder holder) implements AutoCloseable {
    @Override
    public void close() throws Exception {

    }
}
