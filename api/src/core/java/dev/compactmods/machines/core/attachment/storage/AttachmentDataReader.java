package dev.compactmods.machines.core.attachment.storage;

import net.neoforged.neoforge.attachment.AttachmentType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public interface AttachmentDataReader {
    /**
     * Returns {@code true} if there is any data attachments, {@code false} otherwise.
     */
    boolean hasAttachments();

    /**
     * Returns {@code true} if there is a data attachment of the give type, {@code false} otherwise.
     */
    boolean hasData(AttachmentType<?> type);

    /**
     * Returns {@code true} if there is a data attachment of the give type, {@code false} otherwise.
     */
    default <T> boolean hasData(Supplier<AttachmentType<T>> type) {
        //Note: The unused T generic is necessary so that DeferredHolders can be properly matched as a Supplier
        return hasData(type.get());
    }

    /**
     * {@return the data attachment of the given type}
     *
     * <p>If there is no data attachment of the given type, <b>the default value is stored in this holder and returned.</b>
     */
    <T> T getData(AttachmentType<T> type);

    /**
     * {@return the data attachment of the given type}
     *
     * <p>If there is no data attachment of the given type, <b>the default value is stored in this holder and returned.</b>
     */
    default <T> T getData(Supplier<AttachmentType<T>> type) {
        return getData(type.get());
    }

    /**
     * {@return an optional possibly containing a data attachment value of the given type}
     *
     * <p>If there is no data attachment of the given type, an empty optional is returned.
     */
    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return Optional.ofNullable(getExistingDataOrNull(type));
    }

    /**
     * {@return an optional possibly containing a data attachment value of the given type}
     *
     * <p>If there is no data attachment of the given type, an empty optional is returned.
     */
    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return getExistingData(type.get());
    }

    /**
     * @return an existing data attachment value of the given type, or null if there is no data attachment of the given type
     */
    @Nullable
    <T> T getExistingDataOrNull(AttachmentType<T> type);

    /**
     * @return an existing data attachment value of the given type, or null if there is no data attachment of the given type
     */
    @Nullable
    default <T> T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return getExistingDataOrNull(type.get());
    }
}
