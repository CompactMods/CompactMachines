package dev.compactmods.machines.core.attachment;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public interface IForwardingAttachmentHolder extends IAttachmentHolder {

    Supplier<IAttachmentHolder> getAttachmentHolder();

    @Override
    default boolean hasAttachments() {
        return getAttachmentHolder().get().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> attachmentType) {
        return getAttachmentHolder().get().hasData(attachmentType);
    }

    @Override
    default <T> T getData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().get().getData(attachmentType);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().get().getExistingData(attachmentType);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> attachmentType, T t) {
        return getAttachmentHolder().get().setData(attachmentType, t);
    }

    @Override
    default <T> @Nullable T removeData(AttachmentType<T> attachmentType) {
        return getAttachmentHolder().get().removeData(attachmentType);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return getAttachmentHolder().get().getExistingDataOrNull(type);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(@NotNull AttachmentType<T> attachmentType) {
        return getAttachmentHolder().get().getExistingDataOrNull(attachmentType);
    }
}
