package dev.compactmods.machines.core.attachment.storage;

import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class SimpleAttachmentDataStorage implements AttachmentDataStorage, ValidatingAttachmentDataStorage {
    private final IAttachmentHolder holder;
    protected final Map<AttachmentType<?>, Object> attachments;

    // Told you we need a better codec/attachment system
    // final Function<IAttachmentHolder, T> defaultValueSupplier;
    private static final Field DEFAULT_VALUE_SUPPLIER = ObfuscationReflectionHelper.findField(AttachmentType.class, "defaultValueSupplier");

    public SimpleAttachmentDataStorage(IAttachmentHolder holder) {
        this.holder = holder;
        this.attachments = new IdentityHashMap<>();
    }

    @Override
    public final boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    @Override
    public final boolean hasData(AttachmentType<?> type) {
        validateAttachmentType(type);
        return attachments.containsKey(type);
    }

    @Override
    public final <T> T getData(AttachmentType<T> type) {
        validateAttachmentType(type);
        //noinspection unchecked
        return (T) attachments.computeIfAbsent(type, t -> {
            final var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(t);
            try {
                @SuppressWarnings("unchecked")
                final var supplier = (Function<IAttachmentHolder, T>) DEFAULT_VALUE_SUPPLIER.get(t);
                return supplier.apply(holder);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Nullable
    public <T> T getExistingDataOrNull(AttachmentType<T> type) {
        validateAttachmentType(type);
        //noinspection unchecked
        return (T) this.attachments.get(type);
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T setData(AttachmentType<T> type, T data) {
        validateAttachmentType(type);
        Objects.requireNonNull(data);
        //noinspection unchecked
        return (T) attachments.put(type, data);
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T removeData(AttachmentType<T> type) {
        validateAttachmentType(type);
        //noinspection unchecked
        return (T) attachments.remove(type);
    }

    @Override
    public int size() {
        return attachments.size();
    }

    @Override
    public Stream<AttachmentType<?>> storedTypes() {
        return attachments.keySet().stream();
    }

    @Override
    public void putDataNoSync(AttachmentType<?> type, Object copy) {
        attachments.put(type, copy);
    }
}
