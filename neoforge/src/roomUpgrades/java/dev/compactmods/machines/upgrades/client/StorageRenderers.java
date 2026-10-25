package dev.compactmods.machines.upgrades.client;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.robotgryphon.screenlib.nodes.api.render.PropertyValueRenderer;
import dev.robotgryphon.screenlib.nodes.api.render.PropertyValueRenderers;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Room upgrade value renderers for screenlib node properties. Register on the client only (the
 * renderer registry exists there); the keys are safe anywhere, e.g. datagen appearances.
 */
public final class StorageRenderers {

    private StorageRenderers() {}

    /** Item handler values of any kind, including the debug storages; see {@link StorageRenderer}. */
    public static final ResourceKey<PropertyValueRenderer<?>> STORAGE =
            ResourceKey.create(PropertyValueRenderers.REGISTRY_KEY, CompactMachinesCore.identifier("storage"));

    public static void register(IEventBus modBus) {
        final DeferredRegister<PropertyValueRenderer<?>> renderers =
                DeferredRegister.create(PropertyValueRenderers.REGISTRY_KEY, CompactMachinesCore.MOD_ID);
        renderers.register(STORAGE.identifier().getPath(), StorageRenderer::new);
        renderers.register(modBus);
    }
}
