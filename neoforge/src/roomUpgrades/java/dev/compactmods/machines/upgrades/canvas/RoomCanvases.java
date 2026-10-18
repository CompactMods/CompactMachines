package dev.compactmods.machines.upgrades.canvas;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import dev.compactmods.machines.upgrades.system.RoomSystems;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.robotgryphon.screenlib.graph.schema.properties.DataKey;
import dev.robotgryphon.screenlib.nodes.api.NodeReference;
import dev.robotgryphon.screenlib.nodes.api.ProvidedNodeReference;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasDefinition;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasSupplier;
import dev.robotgryphon.screenlib.nodes.api.state.CanvasState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Each room's upgrade canvas: a screenlib node canvas, edited from the machine screen's room upgrades
 * panel. It knows nothing about specific upgrades — it's built from the room's enabled upgrades'
 * {@link CanvasUpgradeComponent}s, which provide their nodes and keep their own state. Query with
 * {@code room.getCapability(RoomCanvases.CANVAS)}; the layout persists in the {@link #STATE} room
 * attachment.
 */
public final class RoomCanvases {

    private RoomCanvases() {}

    public static final RoomCapability<CanvasSupplier, Void> CANVAS =
            RoomCapability.createVoid(CompactMachinesCore.identifier("upgrade_canvas"), CanvasSupplier.class);

    /** Persisted room attachment: the canvas layout (nodes and connections). */
    public static final Supplier<AttachmentType<CanvasState>> STATE =
            Rooms.ATTACHMENT_TYPES.register("upgrade_canvas", () -> AttachmentType
                    .builder(CanvasState::empty)
                    .serialize(CanvasState.MAP_CODEC)
                    .build());

    // One canvas per (server, room), so watchers and open editors share it. Weak server keys drop
    // entries when an integrated server is collected; room codes are unique per server.
    private static final Map<MinecraftServer, Map<String, CanvasSupplier>> CANVASES = new WeakHashMap<>();

    /** Loads the class so {@link #STATE} is registered. Call during mod construction. */
    public static void init() {
    }

    /** Registers the {@link #CANVAS} provider. Call during {@code RegisterCapabilitiesEvent}. */
    public static void registerProviders() {
        RoomCapability.register(CANVAS, (server, room, _) ->
                CANVASES.computeIfAbsent(server, s -> new HashMap<>())
                        .computeIfAbsent(room.code(), code -> build(server, room)));
    }

    /**
     * Drops {@code room}'s canvas so the next query rebuilds it — call when its enabled upgrades
     * change. Menus already open keep editing the old one until closed.
     */
    public static void invalidate(MinecraftServer server, RoomInstance room) {
        final var canvases = CANVASES.get(server);
        if (canvases != null)
            canvases.remove(room.code());
    }

    private static CanvasSupplier build(MinecraftServer server, RoomInstance room) {
        final var definition = new CanvasDefinition(room.getData(STATE));
        final var context = new Context(room, definition);

        final var upgrades = server.registryAccess().lookupOrThrow(CompiledRoomUpgrade.REGISTRY_KEY);
        room.getData(RoomSystems.ENABLED_UPGRADES).stream()
                .map(upgrades::getValue)
                .filter(upgrade -> upgrade != null)
                .flatMap(upgrade -> upgrade.components().stream())
                .filter(CanvasUpgradeComponent.class::isInstance)
                .map(CanvasUpgradeComponent.class::cast)
                .forEach(component -> component.contribute(context));

        // Nodes must all be provided before the supplier exists; watches attach to it afterwards
        final var supplier = CanvasSupplier.of(definition)
                .onChanged(() -> room.setData(STATE, definition.state()));
        context.watches.forEach(watch -> watch.attach(supplier));

        supplier.refresh(server);
        return supplier;
    }

    private static final class Context implements RoomCanvasContext {
        private final RoomInstance room;
        private final CanvasDefinition definition;
        private final List<Watch<?>> watches = new ArrayList<>();

        Context(RoomInstance room, CanvasDefinition definition) {
            this.room = room;
            this.definition = definition;
        }

        @Override
        public RoomInstance room() {
            return room;
        }

        @Override
        public ProvidedNodeReference provide(ResourceKey<NodeDefinition> type, Map<String, Object> initialValues) {
            return definition.provided(type).orElseGet(() -> definition.provide(type, initialValues));
        }

        @Override
        public <T> void watch(NodeReference node, DataKey<T> key, Consumer<Optional<T>> listener) {
            watches.add(new Watch<>(node, key, listener));
        }
    }

    private record Watch<T>(NodeReference node, DataKey<T> key, Consumer<Optional<T>> listener) {
        void attach(CanvasSupplier supplier) {
            supplier.watch(node, key, listener);
        }
    }
}
