package dev.compactmods.machines.api.room.capability;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomCapability<T, Ctx extends @Nullable Object> extends BaseCapability<T, Ctx> {

    private static final CapabilityRegistry<RoomCapability<?, ?>> registry = new CapabilityRegistry<>(RoomCapability::new);
    final Set<IRoomCapabilityProvider<T, Ctx>> providers = new HashSet<>();

    private RoomCapability(Identifier identifier, Class<T> aClass, Class<Ctx> ctxClass) {
        super(identifier, aClass, ctxClass);
    }

    public static <T, C> RoomCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        //noinspection unchecked,rawtypes
        return (RoomCapability) registry.create(name, typeClass, contextClass);
    }

    public static <T> RoomCapability<T, Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static synchronized List<RoomCapability<?, ?>> getAll() {
        return registry.getAll();
    }

    public static <T, C> void register(RoomCapability<T, C> capability, IRoomCapabilityProvider<T, C> provider) {
        capability.providers.add(provider);
    }

    @ApiStatus.Internal
    public @Nullable T getCapability(MinecraftServer server, RoomInstance room, @Nullable Ctx ctx) {
        for (IRoomCapabilityProvider<T, Ctx> provider : providers) {
            T ret = provider.getCapability(server, room, ctx);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }
}
