package dev.compactmods.machines.upgrades.storage.tunnel;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.Map;
import java.util.Optional;

/**
 * A room's named storages: creating, editing, binding and resolving them. Obtained per room via
 * {@code room.getCapability(StorageCapabilities.NAMED_STORAGES)}.
 *
 * <p>Definitions persist in {@link RoomNamedStorages}. Contents are memory only: each storage's
 * handler is built on first use from its definition (see {@link RoomStorageHandlers}) and kept for
 * the server's lifetime, so a restart brings storages back empty but with their capacity.
 */
public final class NamedStorageService {

    public static final int MAX_NAME_LENGTH = 32;

    private final MinecraftServer server;
    private final RoomInstance room;
    private final Map<String, ResourceHandler<?>> live;

    /** @param live this room's live handlers, by storage name; owned by the capability provider */
    public NamedStorageService(MinecraftServer server, RoomInstance room, Map<String, ResourceHandler<?>> live) {
        this.server = server;
        this.room = room;
        this.live = live;
    }

    /** The outcome of a change: the storage as it now stands, or why the change was refused. */
    public sealed interface Result {
        record Success(RoomStorageDefinition storage) implements Result {}

        record Failure(Component error) implements Result {}
    }

    public RoomNamedStorages definitions() {
        return RoomNamedStorages.get(room);
    }

    public Optional<RoomStorageDefinition> definition(String name) {
        return definitions().get(name);
    }

    public Optional<RoomStorageDefinition> boundTo(BlockPos tunnel) {
        return definitions().boundTo(tunnel);
    }

    public Optional<Holder.Reference<TunnelType>> tunnelType(ResourceKey<TunnelType> key) {
        return server.registryAccess().lookupOrThrow(TunnelType.REGISTRY_KEY).get(key);
    }

    /** {@return the storage's capacity}, or 0 if it or its tunnel type is gone */
    public int capacity(RoomStorageDefinition def) {
        return tunnelType(def.type()).map(t -> t.value().capacity(def.containers())).orElse(0);
    }

    /** {@return the live handler for the named storage}, built on first use */
    public Optional<ResourceHandler<?>> handler(String name) {
        final var existing = live.get(name);
        if (existing != null)
            return Optional.of(existing);

        return definition(name).flatMap(this::build).map(handler -> {
            final var stable = RoomStorageHandlers.stable(handler);
            live.put(name, stable);
            return stable;
        });
    }

    /**
     * {@return the live handler for the named storage, typed to its resource}, or empty if there is no
     * such storage or it holds a different resource type
     */
    @SuppressWarnings("unchecked")
    public <T extends Resource> Optional<ResourceHandler<T>> handler(String name, Class<T> resourceClass) {
        final var matches = definition(name)
                .flatMap(def -> tunnelType(def.type()))
                .flatMap(type -> type.value().resolveResourceType())
                .filter(resourceClass::equals)
                .isPresent();

        return matches ? handler(name).map(h -> (ResourceHandler<T>) h) : Optional.empty();
    }

    /** {@return why {@code name} can't be used for a new storage}, or empty if it can */
    public Optional<Component> checkName(String name) {
        if (name.isBlank())
            return Optional.of(TunnelTranslations.Errors.EMPTY_NAME.get());
        if (name.length() > MAX_NAME_LENGTH)
            return Optional.of(TunnelTranslations.Errors.NAME_TOO_LONG.apply(MAX_NAME_LENGTH));
        return Optional.empty();
    }

    /** {@return why {@code type} can't be used to create a storage}, or empty if it can */
    public Optional<Component> checkType(ResourceKey<TunnelType> type) {
        final var usable = tunnelType(type)
                .map(Holder.Reference::value)
                .filter(t -> t.resolveResourceType().isPresent() && t.resolveStorageType().isPresent())
                .filter(t -> RoomStorageHandlers.supports(t.resourceType()))
                .isPresent();

        return usable ? Optional.empty() : Optional.of(TunnelTranslations.Errors.UNKNOWN_TYPE.get());
    }

    /**
     * Creates a storage bound to {@code tunnel}, or binds the existing unbound storage of the same name
     * and type, adding {@code containers} to it.
     */
    public Result create(String name, ResourceKey<TunnelType> type, int containers, BlockPos tunnel) {
        final var nameError = checkName(name);
        if (nameError.isPresent())
            return new Result.Failure(nameError.get());

        final var typeError = checkType(type);
        if (typeError.isPresent())
            return new Result.Failure(typeError.get());

        final var existing = definition(name);
        if (existing.isPresent()) {
            final var def = existing.get();
            if (def.isBound() || !def.type().equals(type))
                return new Result.Failure(TunnelTranslations.Errors.DUPLICATE_NAME.apply(name));

            final var rebound = def.withContainers(def.containers() + containers).withTunnel(Optional.of(tunnel));
            store(def, rebound);
            return new Result.Success(rebound);
        }

        if (containers < 1) {
            final var container = tunnelType(type).orElseThrow().value().container().value().getDefaultInstance().getHoverName();
            return new Result.Failure(TunnelTranslations.Errors.MISSING_CONTAINER.apply(container));
        }

        final var created = new RoomStorageDefinition(name, type, containers, Optional.of(tunnel));
        RoomNamedStorages.update(room, s -> s.with(created));
        return new Result.Success(created);
    }

    /** Renames a storage and/or adds containers to it. Its tunnel type can't change. */
    public Result edit(String name, String newName, int addedContainers) {
        final var existing = definition(name);
        if (existing.isEmpty())
            return new Result.Failure(TunnelTranslations.Errors.UNKNOWN_STORAGE.apply(name));

        final var def = existing.get();
        if (!newName.equals(name)) {
            final var nameError = checkName(newName);
            if (nameError.isPresent())
                return new Result.Failure(nameError.get());
            if (definition(newName).isPresent())
                return new Result.Failure(TunnelTranslations.Errors.DUPLICATE_NAME.apply(newName));
        }

        final var edited = def.withName(newName).withContainers(def.containers() + Math.max(0, addedContainers));
        store(def, edited);
        return new Result.Success(edited);
    }

    /** Unbinds the storage from its tunnel, keeping its data. {@return the storage as it was}, if bound there */
    public Optional<RoomStorageDefinition> unbind(BlockPos tunnel) {
        final var bound = boundTo(tunnel);
        bound.ifPresent(def -> RoomNamedStorages.update(room, s -> s.with(def.withTunnel(Optional.empty()))));
        return bound;
    }

    /**
     * Stores {@code updated} in place of {@code previous}, carrying its live handler over. A grown storage
     * keeps the same handler object (see {@link RoomStorageHandlers.Stable}), so whoever holds it keeps
     * seeing the storage.
     */
    private void store(RoomStorageDefinition previous, RoomStorageDefinition updated) {
        RoomNamedStorages.update(room, s -> s.renamed(previous.name(), updated));

        final var handler = live.remove(previous.name());
        if (handler == null)
            return;

        if (updated.containers() != previous.containers() && handler instanceof RoomStorageHandlers.Stable<?> stable)
            build(updated).ifPresent(stable::grow);

        live.put(updated.name(), handler);
    }

    private Optional<ResourceHandler<?>> build(RoomStorageDefinition def) {
        return tunnelType(def.type())
                .map(Holder.Reference::value)
                .flatMap(type -> RoomStorageHandlers.create(type.resourceType(), type.capacity(def.containers())));
    }
}
