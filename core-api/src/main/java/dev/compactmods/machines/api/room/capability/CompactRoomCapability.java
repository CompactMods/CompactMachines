package dev.compactmods.machines.api.room.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompactRoomCapability<T, Ctx extends @Nullable Object> extends BaseCapability<T, Ctx> {

    private static final CapabilityRegistry<CompactRoomCapability<?, ?>> registry = new CapabilityRegistry<>(CompactRoomCapability::new);
    final Set<IRoomCapabilityProvider<T, Ctx>> providers = new HashSet<>();

    private CompactRoomCapability(ResourceLocation resourceLocation, Class<T> aClass, Class<Ctx> ctxClass) {
        super(resourceLocation, aClass, ctxClass);
    }

    public static <T, C> CompactRoomCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        //noinspection unchecked,rawtypes
        return (CompactRoomCapability) registry.create(name, typeClass, contextClass);
    }

    public static <T> CompactRoomCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static synchronized List<CompactRoomCapability<?, ?>> getAll() {
        return registry.getAll();
    }

    public static <T, C> void register(CompactRoomCapability<T, C> capability, IRoomCapabilityProvider<T, C> provider) {
        capability.providers.add(provider);
    }

    @ApiStatus.Internal
    public @Nullable T getCapability(MinecraftServer server, String roomCode, @Nullable Ctx ctx) {
        for (IRoomCapabilityProvider<T, Ctx> provider : providers) {
            T ret = provider.getCapability(server, roomCode, ctx);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }
}
