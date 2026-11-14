package dev.compactmods.machines.machine.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.robotgryphon.screenlib.graph.schema.types.FluidHandlerAccess;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import dev.robotgryphon.screenlib.nodes.api.canvas.NodeValues;
import net.minecraft.core.Direction;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Which handlers a machine exposes on each side, built from its configuration canvas's
 * {@link MachineSideNode}s. When several nodes bind the same side and resource, the one with the lowest
 * node id wins, so the result doesn't depend on evaluation order.
 *
 * @param items  Item handlers, by side.
 * @param fluids Fluid handlers, by side.
 */
public record MachineSideBindings(Map<Direction, ItemHandlerAccess> items, Map<Direction, FluidHandlerAccess> fluids) {

    public static final MachineSideBindings EMPTY = new MachineSideBindings(Map.of(), Map.of());

    public static final MapCodec<MachineSideBindings> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.unboundedMap(Direction.CODEC, ItemHandlerAccess.CODEC).optionalFieldOf("items", Map.of()).forGetter(MachineSideBindings::items),
            Codec.unboundedMap(Direction.CODEC, FluidHandlerAccess.CODEC).optionalFieldOf("fluids", Map.of()).forGetter(MachineSideBindings::fluids)
    ).apply(i, MachineSideBindings::new));

    public MachineSideBindings {
        items = Map.copyOf(items);
        fluids = Map.copyOf(fluids);
    }

    /** {@return the bindings made by {@code nodes}}, a {@link MachineSideNode} snapshot by node id */
    public static MachineSideBindings from(Map<UUID, NodeValues> nodes) {
        final var items = new EnumMap<Direction, ItemHandlerAccess>(Direction.class);
        final var fluids = new EnumMap<Direction, FluidHandlerAccess>(Direction.class);

        nodes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(Map.Entry::getValue)
                .forEach(node -> {
                    for (Direction side : Direction.values()) {
                        node.value(MachineSideNode.input(side)).ifPresent(value -> {
                            if (value instanceof ItemHandlerAccess item)
                                items.putIfAbsent(side, item);
                            else if (value instanceof FluidHandlerAccess fluid)
                                fluids.putIfAbsent(side, fluid);
                        });
                    }
                });

        return new MachineSideBindings(items, fluids);
    }
}
