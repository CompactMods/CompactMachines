package dev.compactmods.machines.core.attachment.storage;

import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public interface AttachmentDataWriter {
    /**
     * Sets the data attachment of the given type.
     *
     * @return the previous value for that attachment type, if any, or {@code null} if there was none
     */
    @MustBeInvokedByOverriders
    <T> @Nullable T setData(AttachmentType<T> type, T data);

    /**
     * Sets the data attachment of the given type.
     *
     * @return the previous value for that attachment type, if any, or {@code null} if there was none
     */
    default <T> @Nullable T setData(Supplier<AttachmentType<T>> type, T data) {
        return setData(type.get(), data);
    }

    /**
     * Removes the data attachment of the given type.
     *
     * @return the previous value for that attachment type, if any, or {@code null} if there was none
     */
    @MustBeInvokedByOverriders
    @Nullable
    <T> T removeData(AttachmentType<T> type);

    /**
     * Removes the data attachment of the given type.
     *
     * @return the previous value for that attachment type, if any, or {@code null} if there was none
     */
    default <T> @Nullable T removeData(Supplier<AttachmentType<T>> type) {
        return removeData(type.get());
    }
}
