package dev.compactmods.machines.datagen.debug;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.debug.DebugStorages;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinitionBuilder;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataTypes;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

/**
 * The {@code debug_nodes} built-in datapack: canvas nodes for testing upgrades, offering
 * {@link DebugStorages}. Only registered (and always on) outside production; see {@code CMFeaturePacks}.
 */
public final class DebugNodesDatapack {

    private DebugNodesDatapack() {}

    public static final String PACK = "debug_nodes";

    public static final ResourceKey<NodeDefinition> INFINITE_ITEMS = key("debug_infinite_items");
    public static final ResourceKey<NodeDefinition> VOID_ITEMS = key("debug_void_items");

    private static ResourceKey<NodeDefinition> key(String name) {
        return ResourceKey.create(NodeDefinition.REGISTRY_KEY, CompactMachinesCore.identifier(name));
    }

    public static void generatePack(GatherDataEvent event) {
        final var pack = event.getGenerator().getBuiltinDatapack(true, CompactMachinesCore.MOD_ID, PACK);
        pack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output,
                Component.literal("Compact Machines debugging nodes (development only).")));
        pack.addProvider(output -> DatapackBuiltinEntriesProvider.forWorldLayer(output, "Debug nodes",
                event.getWorldLookupProvider(),
                new RegistrySetBuilder().add(NodeDefinition.REGISTRY_KEY, DebugNodesDatapack::nodes),
                Set.of(CompactMachinesCore.MOD_ID)));
    }

    private static void nodes(BootstrapContext<NodeDefinition> ctx) {
        ctx.register(INFINITE_ITEMS, new NodeDefinitionBuilder()
                .addProperty(DebugStorages.ITEM.name(), prop -> prop.type(GraphDataTypes.ITEM).input())
                .addProperty("storage", prop -> prop
                        .output()
                        .computed(DebugStorages.INFINITE_ITEMS)
                        .data(DebugStorages.ITEM, DebugStorages.ITEM.name()))
                .build());

        ctx.register(VOID_ITEMS, new NodeDefinitionBuilder()
                .addProperty("storage", prop -> prop
                        .output()
                        .computed(DebugStorages.VOID_ITEMS))
                .build());
    }
}
