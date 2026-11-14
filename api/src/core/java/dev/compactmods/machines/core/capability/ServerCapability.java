package dev.compactmods.machines.core.capability;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerCapability<T, Ctx extends @Nullable Object> extends BaseCapability<T, Ctx> {

    private static final CapabilityRegistry<ServerCapability<?, ?>> registry = new CapabilityRegistry<>(ServerCapability::new);
    final Set<IServerCapabilityProvider<T, Ctx>> providers = new HashSet<>();

    private ServerCapability(Identifier identifier, Class<T> aClass, Class<Ctx> ctxClass) {
        super(identifier, aClass, ctxClass);
    }

    public static <T, C> ServerCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        //noinspection unchecked
        return (ServerCapability<T, C>) registry.create(name, typeClass, contextClass);
    }

    public static <T> ServerCapability<T, Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static synchronized List<ServerCapability<?, ?>> getAll() {
        return registry.getAll();
    }

    public static <T, C> void register(ServerCapability<T, C> capability, IServerCapabilityProvider<T, C> provider) {
        capability.providers.add(provider);
    }

    public static <T, C> void registerVoid(ServerCapability<T, Void> capability, IServerCapabilityProvider<T, Void> provider) {
        capability.providers.add(provider);
    }

    @ApiStatus.Internal
    public @Nullable T getCapability(MinecraftServer server) {
        for (IServerCapabilityProvider<T, Ctx> provider : providers) {
            T ret = provider.getCapability(server, null);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }

    @ApiStatus.Internal
    public @Nullable T getCapability(MinecraftServer server, @Nullable Ctx ctx) {
        for (IServerCapabilityProvider<T, Ctx> provider : providers) {
            T ret = provider.getCapability(server, ctx);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }
}
