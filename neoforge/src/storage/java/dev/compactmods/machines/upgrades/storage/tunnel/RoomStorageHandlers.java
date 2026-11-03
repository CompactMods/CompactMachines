package dev.compactmods.machines.upgrades.storage.tunnel;

import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;

/**
 * Factories for the in-memory handlers that back room-held named storages, keyed by
 * {@link ResourceTypes resource type}.
 *
 * <p>A stopgap until named storages are backed by real external storage: each factory takes the
 * storage's capacity ({@link TunnelType#capacity(int)}) and builds an empty handler of that size.
 * A tunnel type whose resource type has no factory here can't be used to create a storage.
 */
public final class RoomStorageHandlers {

    private static final Map<ResourceKey<Class<? extends Resource>>, IntFunction<? extends ResourceHandler<?>>> FACTORIES =
            new ConcurrentHashMap<>();

    static {
        register(ResourceTypes.ITEM.getKey(), ItemStacksResourceHandler::new);
        register(ResourceTypes.FLUID.getKey(), capacity -> new FluidStacksResourceHandler(1, capacity * FluidType.BUCKET_VOLUME));
    }

    private RoomStorageHandlers() {}

    /** Registers the handler factory for a resource type, replacing any existing one. */
    public static void register(ResourceKey<Class<? extends Resource>> resourceType,
                                IntFunction<? extends ResourceHandler<?>> factory) {
        FACTORIES.put(resourceType, factory);
    }

    public static boolean supports(ResourceKey<Class<? extends Resource>> resourceType) {
        return FACTORIES.containsKey(resourceType);
    }

    /** {@return a new, empty handler of the given capacity}, or empty if the resource type has no factory */
    public static Optional<ResourceHandler<?>> create(ResourceKey<Class<? extends Resource>> resourceType, int capacity) {
        return Optional.ofNullable(FACTORIES.get(resourceType)).map(f -> f.apply(capacity));
    }

    /** {@return {@code handler}, wrapped so it can later be {@linkplain Stable#grow grown} in place} */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Stable<?> stable(ResourceHandler<?> handler) {
        return new Stable(new AtomicReference<>(handler));
    }

    /**
     * A storage's handler as everyone outside sees it: a fixed object, delegating to the current backing
     * handler. Growing the storage swaps the backing handler, so pipes and machines that cached this one
     * keep working without looking it up again.
     */
    public static final class Stable<T extends Resource> extends DelegatingResourceHandler<T> {
        private final AtomicReference<ResourceHandler<T>> target;

        private Stable(AtomicReference<ResourceHandler<T>> target) {
            super(target::get);
            this.target = target;
        }

        /** Moves everything into {@code bigger} and delegates to it from now on. */
        @SuppressWarnings("unchecked")
        public void grow(ResourceHandler<?> bigger) {
            copyInto(target.get(), bigger);
            target.set((ResourceHandler<T>) bigger);
        }
    }

    /**
     * Moves everything in {@code from} into the same slots of {@code to}. Used when a storage grows:
     * the new handler has the same layout with more room, so every slot fits.
     */
    @SuppressWarnings("unchecked")
    public static void copyInto(ResourceHandler<?> from, ResourceHandler<?> to) {
        copyTyped((ResourceHandler<Resource>) from, (ResourceHandler<Resource>) to);
    }

    private static <T extends Resource> void copyTyped(ResourceHandler<T> from, ResourceHandler<T> to) {
        try (var tx = Transaction.openRoot()) {
            for (int i = 0; i < from.size() && i < to.size(); i++) {
                final T resource = from.getResource(i);
                if (resource.isEmpty())
                    continue;

                to.insert(i, resource, from.getAmountAsInt(i), tx);
            }
            tx.commit();
        }
    }
}
