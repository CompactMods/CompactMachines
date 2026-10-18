package dev.compactmods.machines.upgrades.canvas;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.robotgryphon.screenlib.graph.schema.properties.DataKey;
import dev.robotgryphon.screenlib.nodes.api.NodeReference;
import dev.robotgryphon.screenlib.nodes.api.ProvidedNodeReference;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** What a {@link CanvasUpgradeComponent} can do while a room's upgrade canvas is being built. */
public interface RoomCanvasContext {

    RoomInstance room();

    /**
     * Offers a node of {@code type} on the canvas, for players to place. Several upgrades may provide
     * the same type; they share the one node.
     */
    default ProvidedNodeReference provide(ResourceKey<NodeDefinition> type) {
        return provide(type, Map.of());
    }

    /**
     * As {@link #provide(ResourceKey)}, with property values the node starts with when placed. If the
     * type is already provided, the first provider's values stand.
     */
    ProvidedNodeReference provide(ResourceKey<NodeDefinition> type, Map<String, Object> initialValues);

    /**
     * Calls {@code listener} with {@code node}'s {@code key} value whenever it changes, and once when
     * the canvas is built — empty while unset or the node isn't placed.
     */
    <T> void watch(NodeReference node, DataKey<T> key, Consumer<Optional<T>> listener);
}
