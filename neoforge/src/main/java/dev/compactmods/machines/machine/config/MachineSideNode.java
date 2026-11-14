package dev.compactmods.machines.machine.config;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import dev.compactmods.machines.upgrades.storage.nodes.StorageNodes;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinitionBuilder;
import dev.robotgryphon.screenlib.graph.schema.properties.TypeFrom;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Locale;

/**
 * Exposes handlers on the machine block's sides. Its {@code resource_type} picks what it binds — items
 * or fluids — and retypes its six face inputs (one per {@link Direction}, named after it) to match. Place
 * one per resource type to expose several on the same face. Only placeable on the machine configuration
 * canvas.
 */
public final class MachineSideNode {

    private MachineSideNode() {}

    public static final ResourceKey<NodeDefinition> KEY =
            ResourceKey.create(NodeDefinition.REGISTRY_KEY, CompactMachinesCore.identifier("machine_side"));

    public static final String RESOURCE_TYPE = "resource_type";

    /** {@return the name of the face input for {@code side}} */
    public static String input(Direction side) {
        return side.getSerializedName().toLowerCase(Locale.ROOT);
    }

    public static NodeDefinition definition() {
        final var resourceType = StorageNodes.RESOURCE_TYPE.get();
        final var cases = List.of(
                TypeFrom.Case.of(resourceType, ResourceTypes.ITEM.getKey(), GraphDataTypes.ITEM_HANDLER),
                TypeFrom.Case.of(resourceType, ResourceTypes.FLUID.getKey(), GraphDataTypes.FLUID_HANDLER));

        final var builder = new NodeDefinitionBuilder()
                .hidden()
                .addProperty(RESOURCE_TYPE, prop -> prop.type(StorageNodes.RESOURCE_TYPE));

        for (Direction side : Direction.values()) {
            builder.addProperty(input(side), prop -> prop
                    .type(GraphDataTypes.ITEM_HANDLER)
                    .wired()
                    .optional()
                    .typeFrom(RESOURCE_TYPE, cases));
        }

        return builder.build();
    }
}
