package dev.compactmods.machines.core.attachment.persistence;

import com.mojang.logging.LogUtils;
import dev.compactmods.machines.core.attachment.storage.AttachmentDataStorage;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValueIOPersistenceHandler implements AttachmentHolderPersistenceHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    // AttachmentType exposes its serializer as a package-private field (no public accessor), so read it reflectively.
    private static final Field SERIALIZER_FIELD = ObfuscationReflectionHelper.findField(AttachmentType.class, "serializer");
    protected final IAttachmentHolder holder;
    protected final AttachmentDataStorage attachmentData;

    public ValueIOPersistenceHandler(IAttachmentHolder holder, AttachmentDataStorage attachmentData) {
        this.holder = holder;
        this.attachmentData = attachmentData;
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<IAttachmentSerializer<T>> getSerializer(AttachmentType<T> type) {
        // type is null for an unknown/unregistered attachment key in saved data; reading the
        // instance field with a null receiver would NPE (uncaught here), so guard it. Callers
        // already handle an empty result by logging and skipping the stale key.
        if (type == null)
            return Optional.empty();

        try {
            return Optional.ofNullable((IAttachmentSerializer<T>) SERIALIZER_FIELD.get(type));
        } catch (IllegalAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void serialize(@NonNull ValueOutput tag) {
        if (!attachmentData.hasAttachments()) return;
        final var types = attachmentData
                .storedTypes()
                .collect(Collectors.toUnmodifiableSet());

        for (var type : types) {
            var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type);
            if (key == null)
                continue;

            getSerializer(type).ifPresent(s -> {
                try {
                    final var serialized = tag.child(key.toString());
                    final var value = attachmentData.getData(type);

                    //noinspection unchecked
                    boolean doSerialise = ((IAttachmentSerializer<Object>) s).write(value, serialized);
                    if (!doSerialise) {
                        tag.discard(key.toString());
                    }
                } catch (Exception exception) {
                    LOGGER.error("Failed to serialize data attachment {}. Skipping.", key, exception);
                }
            });
        }
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #serialize(ValueOutput)}.
     */
    @Override
    public void deserialize(ValueInput input) {
        for (var key : input.keySet()) {
            // Use tryParse to not discard valid attachment type keys, even if there is a malformed key.
            Identifier keyLocation = Identifier.tryParse(key);
            if (keyLocation == null) {
                LOGGER.error("Encountered invalid data attachment key {}. Skipping.", key);
                continue;
            }

            var type = NeoForgeRegistries.ATTACHMENT_TYPES.getValue(keyLocation);
            final var serializer = getSerializer(type);
            if (type == null || serializer.isEmpty()) {
                LOGGER.error("Encountered unknown or non-serializable data attachment {}. Skipping.", key);
                continue;
            }

            try {
                serializer.map(s -> s.read(holder, input.rawChildOrEmpty(key)))
                        .ifPresent(attachment -> attachmentData.putDataNoSync(type, attachment));
            } catch (Exception exception) {
                LOGGER.error("Failed to deserialize data attachment {}. Skipping.", key, exception);
            }
        }
    }
}
