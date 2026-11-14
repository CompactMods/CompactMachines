package dev.compactmods.machines.machine.capability;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.CapabilityRegistry;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class MachineCapability<T, C> extends BaseCapability<T, C> {

    private static final CapabilityRegistry<MachineCapability<?,?>> registry = new CapabilityRegistry<>(MachineCapability::new);
    final Map<String, List<ICapabilityProvider<RoomInstance, C, T>>> providers = new IdentityHashMap<>();

    protected MachineCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    private static <T, C> MachineCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return (MachineCapability<T, C>) registry.create(name, typeClass, contextClass);
    }

    public static <T> MachineCapability<T, Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static <T> MachineCapability<T, GlobalPos> createMachineUnsided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, GlobalPos.class);
    }

    public static <T> MachineCapability<T, GlobalPos> createMachineSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, GlobalPos.class);
    }

    @ApiStatus.Internal
    @Nullable
    public T getCapability(RoomInstance room, C context) {
        for (var provider : providers.getOrDefault(room.code(), List.of())) {
            var ret = provider.getCapability(room, context);
            if (ret != null)
                return ret;
        }
        return null;
    }
}
