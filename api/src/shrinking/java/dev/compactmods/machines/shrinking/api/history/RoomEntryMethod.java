package dev.compactmods.machines.shrinking.api.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public interface RoomEntryMethod {

    interface Type<T extends RoomEntryMethod> {
        ResourceKey<Registry<RoomEntryMethod.Type<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(CompactMachinesCore.identifier("room_entrypoint_types"));

        MapCodec<T> codec();

        record Simple<T extends RoomEntryMethod>(MapCodec<T> codec) implements RoomEntryMethod.Type<T> { }
    }

    @SuppressWarnings("unchecked")
    Codec<RoomEntryMethod> DISPATCH_CODEC =  Codec.lazyInitialized(() -> (Codec<RoomEntryMethod>) BuiltInRegistries.REGISTRY
            .getOptional(Type.REGISTRY_KEY.identifier())
            .map(r -> (Registry<Type<?>>) r)
            .map(Registry::byNameCodec)
            .map(c -> c.dispatchStable(m -> m.type().value(), Type::codec))
            .orElseThrow(() -> new RuntimeException("Registry not registered yet; calling too early?")));

    RoomExitResult exit(MinecraftServer server, ServerPlayer player, RoomInstance leaving);

    Holder<Type<?>> type();

    static <T extends RoomEntryMethod> Supplier<Type<T>> simple(MapCodec<T> codec) {
        return () -> new Type.Simple<>(codec);
    }
}
