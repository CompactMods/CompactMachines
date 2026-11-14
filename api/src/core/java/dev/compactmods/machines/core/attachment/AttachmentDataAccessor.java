package dev.compactmods.machines.core.attachment;

import dev.compactmods.machines.core.attachment.storage.AttachmentDataStorage;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@org.jetbrains.annotations.ApiStatus.OverrideOnly
public interface AttachmentDataAccessor extends AttachmentDataStorage, IAttachmentHolder {
    AttachmentDataStorage dataStorage();

    @Override
    default boolean hasAttachments() {
        return dataStorage().hasAttachments();
    }

    @Override
    default boolean hasData(AttachmentType<?> attachmentType) {
        return dataStorage().hasData(attachmentType);
    }

    @Override
    default <T> boolean hasData(Supplier<AttachmentType<T>> type) {
        return AttachmentDataStorage.super.hasData(type);
    }

    @Override
    default <T> T getData(AttachmentType<T> attachmentType) {
        return dataStorage().getData(attachmentType);
    }

    @Override
    default <T> T getData(Supplier<AttachmentType<T>> type) {
        return dataStorage().getData(type);
    }

    @Override
    default <T> Optional<T> getExistingData(AttachmentType<T> attachmentType) {
        return dataStorage().getExistingData(attachmentType);
    }

    @Override
    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) {
        return dataStorage().getExistingData(type);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(AttachmentType<T> attachmentType) {
        return dataStorage().getExistingDataOrNull(attachmentType);
    }

    @Override
    default <T> @Nullable T getExistingDataOrNull(Supplier<AttachmentType<T>> type) {
        return dataStorage().getExistingDataOrNull(type);
    }

    @Override
    default <T> @Nullable T setData(AttachmentType<T> attachmentType, T t) {
        return dataStorage().setData(attachmentType, t);
    }

    @Override
    @Nullable
    default <T> T setData(Supplier<AttachmentType<T>> type, T data) {
        return dataStorage().setData(type, data);
    }

    @Override
    @Nullable
    default <T> T removeData(AttachmentType<T> type) {
        return dataStorage().removeData(type);
    }

    @Override
    @Nullable
    default <T> T removeData(Supplier<AttachmentType<T>> type) {
        return dataStorage().removeData(type);
    }

    @Override
    default int size() {
        return dataStorage().size();
    }

    @Override
    default Stream<AttachmentType<?>> storedTypes() {
        return dataStorage().storedTypes();
    }

    @Override
    default void putDataNoSync(AttachmentType<?> type, Object copy) {
        dataStorage().putDataNoSync(type, copy);
    }
}
