package dev.compactmods.machines.upgrades.storage;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

/**
 * A resolved, live pairing of a {@link ResourceStoragePosition} and the resource handler found there.
 *
 * <p>This is the product of a {@link ResourceStorageScanner} scan: unlike {@link NamedResourceStorage}
 * (an unresolved, serializable handle), it carries the live {@link ResourceHandler} that was just
 * queried and is only valid for as long as that handler is. Use {@link #named(String)} to turn it
 * into a persistable handle once a user assigns it a name.
 *
 * @param storage Where the handler was found and how to re-resolve it.
 * @param handler The live resource handler queried at {@link #position()}.
 * @param <T>     The resource type held by the handler (e.g. {@code ItemResource}).
 */
public record LocatedResourceStorage<T extends Resource>(
        ResourceStoragePosition<T> storage,
        ResourceHandler<T> handler
) {

    /**
     * {@return an unresolved, nameable handle to this same storage} The live {@link #handler} is
     * dropped; the returned handle re-resolves it on demand via {@link NamedResourceStorage#resolve}.
     */
    public NamedResourceStorage<T> named(String name) {
        return new NamedResourceStorage<>(name, storage);
    }

    public GlobalPos position() {
        return storage.position();
    }

    public @Nullable Direction direction() {
        return storage.direction();
    }

    public ResourceStorageType<T> type() {
        return storage.type();
    }
}
