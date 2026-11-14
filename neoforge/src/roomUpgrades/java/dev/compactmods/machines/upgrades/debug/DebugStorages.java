package dev.compactmods.machines.upgrades.debug;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.robotgryphon.screenlib.graph.schema.properties.DataKey;
import dev.robotgryphon.screenlib.graph.schema.properties.computed.ComputeContext;
import dev.robotgryphon.screenlib.graph.schema.properties.computed.ComputedProperty;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataType;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataTypes;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccessKind;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.VoidingResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.Optional;

/**
 * Debugging storages for the room upgrade canvas: an infinite source of one item, and a void that
 * accepts anything. Both are item handler values (any item handler input takes them), produced by the
 * debug nodes' computed outputs. The kinds and computations are always registered, since screenlib
 * syncs both registries; only the node definitions are dev-only (the {@code debug_nodes} datapack).
 */
public final class DebugStorages {

    private DebugStorages() {}

    private static final DeferredRegister<ItemHandlerAccessKind<?>> KINDS =
            DeferredRegister.create(ItemHandlerAccessKind.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    private static final DeferredRegister<ComputedProperty<?>> COMPUTATIONS =
            DeferredRegister.create(ComputedProperty.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    /** Endless {@code item}. */
    public record InfiniteItems(ItemResource item) implements ItemHandlerAccess {
        static final MapCodec<InfiniteItems> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemResource.CODEC.fieldOf("item").forGetter(InfiniteItems::item)
        ).apply(i, InfiniteItems::new));

        static final StreamCodec<RegistryFriendlyByteBuf, InfiniteItems> STREAM_CODEC =
                ItemResource.STREAM_CODEC.map(InfiniteItems::new, InfiniteItems::item);

        @Override
        public ItemHandlerAccessKind<?> kind() {
            return INFINITE_ITEMS_KIND.get();
        }

        @Override
        public Optional<ResourceHandler<ItemResource>> resolve(MinecraftServer server) {
            return Optional.of(new InfiniteItemHandler(item));
        }
    }

    /** Swallows everything put into it (NeoForge's voiding handler). */
    public record VoidItems() implements ItemHandlerAccess {
        static final VoidItems INSTANCE = new VoidItems();

        @Override
        public ItemHandlerAccessKind<?> kind() {
            return VOID_ITEMS_KIND.get();
        }

        @Override
        public Optional<ResourceHandler<ItemResource>> resolve(MinecraftServer server) {
            return Optional.of(new VoidingResourceHandler<>(ItemResource.EMPTY));
        }
    }

    public static final DeferredHolder<ItemHandlerAccessKind<?>, ItemHandlerAccessKind<InfiniteItems>> INFINITE_ITEMS_KIND =
            KINDS.register("infinite_items", () -> new ItemHandlerAccessKind<>(InfiniteItems.CODEC, InfiniteItems.STREAM_CODEC));

    public static final DeferredHolder<ItemHandlerAccessKind<?>, ItemHandlerAccessKind<VoidItems>> VOID_ITEMS_KIND =
            KINDS.register("void_items", () -> new ItemHandlerAccessKind<>(
                    MapCodec.unit(VoidItems.INSTANCE), StreamCodec.unit(VoidItems.INSTANCE)));

    /** The infinite source node's item; unset makes no storage. */
    public static final DataKey<ItemResource> ITEM = DataKey.required("item", GraphDataTypes.ITEM);

    public static final DeferredHolder<ComputedProperty<?>, ComputedProperty<ItemHandlerAccess>> INFINITE_ITEMS =
            COMPUTATIONS.register("infinite_items", () -> new Storage(List.of(ITEM),
                    context -> context.get(ITEM).filter(item -> !item.isEmpty()).map(InfiniteItems::new)));

    public static final DeferredHolder<ComputedProperty<?>, ComputedProperty<ItemHandlerAccess>> VOID_ITEMS =
            COMPUTATIONS.register("void_items", () -> new Storage(List.of(), context -> Optional.of(VoidItems.INSTANCE)));

    public static void register(IEventBus modBus) {
        KINDS.register(modBus);
        COMPUTATIONS.register(modBus);
    }

    /** A computed item handler output. */
    private record Storage(List<DataKey<?>> requires,
                           java.util.function.Function<ComputeContext, Optional<? extends ItemHandlerAccess>> computation)
            implements ComputedProperty<ItemHandlerAccess> {

        @Override
        public DeferredHolder<GraphDataType<?>, ? extends GraphDataType<ItemHandlerAccess>> type() {
            return GraphDataTypes.ITEM_HANDLER;
        }

        @Override
        public Optional<ItemHandlerAccess> compute(ComputeContext context) {
            return computation.apply(context).map(ItemHandlerAccess.class::cast);
        }
    }
}
