package dev.compactmods.machines.upgrades.storage.nodes;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import dev.robotgryphon.screenlib.graph.schema.types.DataTypeWithDefault;
import dev.robotgryphon.screenlib.graph.schema.types.GraphDataType;
import dev.robotgryphon.screenlib.graph.schema.types.RestrictedDataType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.Collection;
import java.util.List;

/** Graph data types added for named storages and machine sides. */
public final class StorageNodeTypes {

    private StorageNodeTypes() {}

    /** {@code compactmachines:named_storage}: a {@link NamedStorageRef}, picked from canvas choices. */
    public static final class NamedStorage implements GraphDataType<NamedStorageRef> {
        @Override
        public Class<NamedStorageRef> dataType() {
            return NamedStorageRef.class;
        }

        @Override
        public Codec<NamedStorageRef> codec() {
            return NamedStorageRef.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, NamedStorageRef> streamCodec() {
            return NamedStorageRef.STREAM_CODEC;
        }
    }

    /**
     * {@code compactmachines:resource_type}: one of the registered {@link ResourceTypes resource types},
     * by key. Defaults to items.
     */
    public static final class ResourceType
            implements RestrictedDataType<ResourceKey<Class<? extends Resource>>>, DataTypeWithDefault<ResourceKey<Class<? extends Resource>>> {

        private static final Codec<ResourceKey<Class<? extends Resource>>> CODEC = ResourceKey.codec(ResourceTypes.REGISTRY_KEY);
        private static final StreamCodec<io.netty.buffer.ByteBuf, ResourceKey<Class<? extends Resource>>> STREAM_CODEC =
                ResourceKey.streamCodec(ResourceTypes.REGISTRY_KEY);

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Class<ResourceKey<Class<? extends Resource>>> dataType() {
            return (Class) ResourceKey.class;
        }

        @Override
        public Codec<ResourceKey<Class<? extends Resource>>> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ResourceKey<Class<? extends Resource>>> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public Collection<ResourceKey<Class<? extends Resource>>> allowedValues(RegistryAccess registries) {
            return registries.lookup(ResourceTypes.REGISTRY_KEY)
                    .map(types -> types.listElementIds().toList())
                    .orElse(List.of());
        }

        @Override
        public ResourceKey<Class<? extends Resource>> defaultValue() {
            return ResourceTypes.ITEM.getKey();
        }
    }
}
