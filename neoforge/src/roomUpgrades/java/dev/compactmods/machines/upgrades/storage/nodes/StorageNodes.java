package dev.compactmods.machines.upgrades.storage.nodes;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinitionBuilder;
import dev.robotgryphon.screenlib.graph.schema.properties.DataKey;
import dev.robotgryphon.screenlib.graph.schema.properties.computed.ComputeContext;
import dev.robotgryphon.screenlib.graph.schema.properties.computed.ComputedProperty;
import dev.robotgryphon.screenlib.graph.schema.types.FluidHandlerAccess;
import dev.robotgryphon.screenlib.graph.schema.types.FluidHandlerAccessKind;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataType;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataTypes;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccessKind;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Canvas nodes for a room's named storages: {@link #NAMED_ITEM_STORAGE} and {@link #NAMED_FLUID_STORAGE}
 * each pick one of the room's storages (from the canvas's {@linkplain #registerChoices choices}) and
 * output its handler. Usable on any canvas that belongs to a room — the room upgrades canvas and the
 * machine configuration canvas both register the choices.
 */
public final class StorageNodes {

    private StorageNodes() {}

    private static final DeferredRegister<GraphDataType<?>> DATA_TYPES =
            DeferredRegister.create(GraphDataType.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    private static final DeferredRegister<ItemHandlerAccessKind<?>> ITEM_KINDS =
            DeferredRegister.create(ItemHandlerAccessKind.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    private static final DeferredRegister<FluidHandlerAccessKind<?>> FLUID_KINDS =
            DeferredRegister.create(FluidHandlerAccessKind.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    private static final DeferredRegister<ComputedProperty<?>> COMPUTATIONS =
            DeferredRegister.create(ComputedProperty.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    // region Data types

    public static final DeferredHolder<GraphDataType<?>, StorageNodeTypes.NamedStorage> NAMED_STORAGE_TYPE =
            DATA_TYPES.register("named_storage", StorageNodeTypes.NamedStorage::new);

    public static final DeferredHolder<GraphDataType<?>, StorageNodeTypes.ResourceType> RESOURCE_TYPE =
            DATA_TYPES.register("resource_type", StorageNodeTypes.ResourceType::new);

    // endregion

    // region Access kinds

    public static final DeferredHolder<ItemHandlerAccessKind<?>, ItemHandlerAccessKind<NamedStorageAccess.Items>> NAMED_ITEMS_KIND =
            ITEM_KINDS.register("named_storage", () -> new ItemHandlerAccessKind<>(
                    NamedStorageAccess.Items.CODEC, NamedStorageAccess.Items.STREAM_CODEC));

    public static final DeferredHolder<FluidHandlerAccessKind<?>, FluidHandlerAccessKind<NamedStorageAccess.Fluids>> NAMED_FLUIDS_KIND =
            FLUID_KINDS.register("named_storage", () -> new FluidHandlerAccessKind<>(
                    NamedStorageAccess.Fluids.CODEC, NamedStorageAccess.Fluids.STREAM_CODEC));

    // endregion

    /** The storage picked on a named storage node. */
    public static final DataKey<NamedStorageRef> STORAGE = DataKey.required("storage", NAMED_STORAGE_TYPE);

    public static final DeferredHolder<ComputedProperty<?>, ComputedProperty<ItemHandlerAccess>> NAMED_ITEM_HANDLER =
            COMPUTATIONS.register("named_item_storage", () -> new Computed<>(GraphDataTypes.ITEM_HANDLER,
                    context -> context.get(STORAGE).map(NamedStorageAccess.Items::new)));

    public static final DeferredHolder<ComputedProperty<?>, ComputedProperty<FluidHandlerAccess>> NAMED_FLUID_HANDLER =
            COMPUTATIONS.register("named_fluid_storage", () -> new Computed<>(GraphDataTypes.FLUID_HANDLER,
                    context -> context.get(STORAGE).map(NamedStorageAccess.Fluids::new)));

    // region Nodes

    public static final ResourceKey<NodeDefinition> NAMED_ITEM_STORAGE = key("named_item_storage");
    public static final ResourceKey<NodeDefinition> NAMED_FLUID_STORAGE = key("named_fluid_storage");

    /** Choices provider ids: a room's storages holding items / fluids. */
    public static final Identifier ITEM_STORAGES = CompactMachinesCore.identifier("named_storages/item");
    public static final Identifier FLUID_STORAGES = CompactMachinesCore.identifier("named_storages/fluid");

    /** Name of the storage nodes' handler output. */
    public static final String HANDLER = "handler";

    public static NodeDefinition namedItemStorage() {
        return storageNode(ITEM_STORAGES, NAMED_ITEM_HANDLER);
    }

    public static NodeDefinition namedFluidStorage() {
        return storageNode(FLUID_STORAGES, NAMED_FLUID_HANDLER);
    }

    private static NodeDefinition storageNode(Identifier choices, DeferredHolder<ComputedProperty<?>, ? extends ComputedProperty<?>> handler) {
        return new NodeDefinitionBuilder()
                .addProperty(STORAGE.name(), prop -> prop.type(STORAGE.type()).choices(choices))
                .addProperty(HANDLER, prop -> prop
                        .output()
                        .computed(handler)
                        .data(STORAGE, STORAGE.name()))
                .build();
    }

    private static ResourceKey<NodeDefinition> key(String name) {
        return ResourceKey.create(NodeDefinition.REGISTRY_KEY, CompactMachinesCore.identifier(name));
    }

    // endregion

    /**
     * Lets the storage nodes on {@code canvas} pick from {@code room}'s named storages. Call when building
     * a canvas that belongs to a room.
     */
    public static void registerChoices(CanvasDefinition canvas, RoomInstance room) {
        canvas.choices(ITEM_STORAGES, (node, server) -> storagesOf(server, room, ItemResource.class));
        canvas.choices(FLUID_STORAGES, (node, server) -> storagesOf(server, room, FluidResource.class));
    }

    /** {@return references to {@code room}'s storages that hold {@code resource}}, in creation order */
    private static List<NamedStorageRef> storagesOf(MinecraftServer server, RoomInstance room, Class<? extends Resource> resource) {
        final var storages = room.getCapability(StorageCapabilities.NAMED_STORAGES);
        return storages.definitions().storages().values().stream()
                .filter(def -> storages.tunnelType(def.type())
                        .flatMap(type -> type.value().resolveResourceType())
                        .map(resource::equals)
                        .orElse(false))
                .map(def -> new NamedStorageRef(room.code(), def.name()))
                .toList();
    }

    public static void register(IEventBus modBus) {
        DATA_TYPES.register(modBus);
        ITEM_KINDS.register(modBus);
        FLUID_KINDS.register(modBus);
        COMPUTATIONS.register(modBus);
    }

    /** A computed handler output of {@code type}. */
    private record Computed<T>(DeferredHolder<GraphDataType<?>, ? extends GraphDataType<T>> type,
                               Function<ComputeContext, Optional<? extends T>> computation)
            implements ComputedProperty<T> {

        @Override
        public List<DataKey<?>> requires() {
            return List.of(STORAGE);
        }

        @Override
        public Optional<T> compute(ComputeContext context) {
            return computation.apply(context).map(value -> value);
        }
    }
}
