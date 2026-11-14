package dev.compactmods.machines.upgrades.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The location half of a resource storage: where a handler lives, which side it is queried from,
 * and which {@link ResourceStorageType} it belongs to.
 *
 * <p>This is the shared core of {@link NamedResourceStorage} (a named handle) and
 * {@link LocatedResourceStorage} (a resolved, live pairing) — both compose one of these. It owns
 * the on-demand {@link #resolve(MinecraftServer)} and the (de)serialization of the three fields.
 *
 * <p>The handler is not stored here: it is a live, non-serializable object owned by the block.
 * Because generics are erased at runtime and the NeoForge block capabilities are static,
 * per-resource constants (e.g. {@code Capabilities.Item.BLOCK} is fixed to {@code ItemResource}),
 * the {@link ResourceStorageType} that ties this location to a concrete resource type {@code T}
 * must be carried explicitly rather than derived from {@code T}. The type is a registered object,
 * so it is what gets persisted (by id) when this location is serialized.
 *
 * @param position  The world position the backing block resides at.
 * @param direction Optional side to fetch the resource handler for ({@code null} = unsided).
 * @param type      The registered storage kind used to resolve the handler for resource type
 *                  {@code T} (e.g. {@code ResourceStorageTypes.ITEM_BLOCK}).
 * @param <T>       The resource type held by the handler (e.g. {@code ItemResource}).
 */
public record ResourceStoragePosition<T extends Resource>(
        GlobalPos position,
        @Nullable Direction direction,
        ResourceStorageType<T> type
) {

    /**
     * Map codec for the three fields; {@link #direction} is optional. Exposed as a {@link MapCodec}
     * so composing records can inline these keys flatly rather than nesting them.
     */
    public static final MapCodec<ResourceStoragePosition<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            GlobalPos.CODEC.fieldOf("position").forGetter(ResourceStoragePosition::position),
            Direction.CODEC.optionalFieldOf("direction").forGetter(s -> Optional.ofNullable(s.direction())),
            ResourceStorageType.CODEC.fieldOf("type").forGetter(ResourceStoragePosition::type)
    ).apply(inst, ResourceStoragePosition::create));

    public static final Codec<ResourceStoragePosition<?>> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStoragePosition<?>> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, ResourceStoragePosition::position,
            ByteBufCodecs.optional(Direction.STREAM_CODEC), s -> Optional.ofNullable(s.direction()),
            ResourceStorageType.STREAM_CODEC, ResourceStoragePosition::type,
            ResourceStoragePosition::create);

    // The registry only yields ResourceStorageType<?>, so the resource type parameter cannot be
    // recovered statically at decode time; the raw construction reunites the wildcard witness with
    // the location it belongs to.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ResourceStoragePosition<?> create(GlobalPos position, Optional<Direction> direction,
                                                     ResourceStorageType<?> type) {
        return new ResourceStoragePosition(position, direction.orElse(null), type);
    }

    /**
     * Resolves the live resource handler at {@link #position} from the given server.
     *
     * @return the handler, or {@code null} if the dimension is not loaded or no block at the
     * position exposes this storage's capability on the requested {@link #direction}.
     */
    public @Nullable ResourceHandler<T> resolve(MinecraftServer server) {
        final var level = server.getLevel(position.dimension());
        if (level == null)
            return null;

        return level.getCapability(type.blockCapability(), position.pos(), direction);
    }
}
