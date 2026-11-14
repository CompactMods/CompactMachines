package dev.compactmods.machines.upgrades.storage.nodes;

import com.mojang.serialization.MapCodec;
import dev.robotgryphon.screenlib.graph.schema.types.FluidHandlerAccess;
import dev.robotgryphon.screenlib.graph.schema.types.FluidHandlerAccessKind;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccessKind;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;

/** Item and fluid handler values that resolve to a room's named storage. */
public final class NamedStorageAccess {

    private NamedStorageAccess() {}

    static final MapCodec<NamedStorageRef> REF_MAP_CODEC = NamedStorageRef.CODEC.fieldOf("storage");

    /** A named item storage; empty if the room, the storage or its item handler is gone. */
    public record Items(NamedStorageRef storage) implements ItemHandlerAccess {
        static final MapCodec<Items> CODEC = REF_MAP_CODEC.xmap(Items::new, Items::storage);
        static final StreamCodec<RegistryFriendlyByteBuf, Items> STREAM_CODEC = NamedStorageRef.STREAM_CODEC.map(Items::new, Items::storage);

        @Override
        public ItemHandlerAccessKind<?> kind() {
            return StorageNodes.NAMED_ITEMS_KIND.get();
        }

        @Override
        public Optional<ResourceHandler<ItemResource>> resolve(MinecraftServer server) {
            return storage.storages(server).flatMap(s -> s.handler(storage.name(), ItemResource.class));
        }
    }

    /** A named fluid storage; empty if the room, the storage or its fluid handler is gone. */
    public record Fluids(NamedStorageRef storage) implements FluidHandlerAccess {
        static final MapCodec<Fluids> CODEC = REF_MAP_CODEC.xmap(Fluids::new, Fluids::storage);
        static final StreamCodec<RegistryFriendlyByteBuf, Fluids> STREAM_CODEC = NamedStorageRef.STREAM_CODEC.map(Fluids::new, Fluids::storage);

        @Override
        public FluidHandlerAccessKind<?> kind() {
            return StorageNodes.NAMED_FLUIDS_KIND.get();
        }

        @Override
        public Optional<ResourceHandler<FluidResource>> resolve(MinecraftServer server) {
            return storage.storages(server).flatMap(s -> s.handler(storage.name(), FluidResource.class));
        }
    }
}
