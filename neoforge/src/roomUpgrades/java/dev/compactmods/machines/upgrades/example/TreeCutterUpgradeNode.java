package dev.compactmods.machines.upgrades.example;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinitionBuilder;
import dev.robotgryphon.screenlib.graph.schema.properties.DataKey;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataTypes;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import net.minecraft.resources.ResourceKey;

/**
 * The tree cutter upgrade's canvas node (copied from screenlib's test mod): whether it's enabled,
 * where it pulls axes from and where it puts drops. Hidden from the add-node palette; the tree cutter upgrade provides it
 * (see {@link TreeCutterUpgradeComponent#contribute}).
 */
public final class TreeCutterUpgradeNode {
    private TreeCutterUpgradeNode() {}

    public static final ResourceKey<NodeDefinition> KEY = ResourceKey.create(NodeDefinition.REGISTRY_KEY,
            CompactMachinesCore.identifier("tree_cutter_upgrade"));

    /** Whether the tree cutter runs; a toggle on the node. */
    public static final DataKey<Boolean> ENABLED = DataKey.optional("enabled", GraphDataTypes.BOOLEAN);
    public static final DataKey<ItemHandlerAccess> TOOLS = DataKey.required("tools", GraphDataTypes.ITEM_HANDLER);
    public static final DataKey<ItemHandlerAccess> DROPS = DataKey.required("drops", GraphDataTypes.ITEM_HANDLER);

    public static NodeDefinition definition() {
        return new NodeDefinitionBuilder()
                .hidden()
                .addProperty(ENABLED.name(), prop -> prop.type(ENABLED.type()).input().optional())
                .addProperty(TOOLS.name(), prop -> prop.type(TOOLS.type()).input())
                .addProperty(DROPS.name(), prop -> prop.type(DROPS.type()).input())
                .build();
    }
}
