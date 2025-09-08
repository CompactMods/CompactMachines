package dev.compactmods.machines.api.attachment;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public interface IForwardingAttachmentHolder extends IAttachmentHolder {

    Supplier<IAttachmentHolder> attachmentHolder();

    @Override
    default boolean hasAttachments() {
        return attachmentHolder().get().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> attachmentType) {
        return attachmentHolder().get().hasData(attachmentType);
    }

    @Override
    default <T> T getData(AttachmentType<T> attachmentType) {
        return attachmentHolder().get().getData(attachmentType);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> attachmentType) {
        return attachmentHolder().get().getExistingData(attachmentType);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> attachmentType, T t) {
        return attachmentHolder().get().setData(attachmentType, t);
    }

    @Override
    default <T> @Nullable T removeData(AttachmentType<T> attachmentType) {
        return attachmentHolder().get().removeData(attachmentType);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return attachmentHolder().get().getExistingDataOrNull(type);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(@NotNull AttachmentType<T> attachmentType) {
        return attachmentHolder().get().getExistingDataOrNull(attachmentType);
    }
}
