package dev.compactmods.machines.upgrades.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fluent builder for discovering resource handlers of a given {@link ResourceStorageType} in a level.
 *
 * <p>Separates a scan into independent steps so the API can grow without new overloads: pick the
 * resource ({@link #forResource}), pick the region ({@link #filterPositions}), optionally narrow the
 * positions ({@link #filterPositions}) and sides ({@link #sides}), then run it against a level
 * ({@link #scan(ServerLevel)}). The capability to probe comes from the type, so the same scan works
 * for items, fluids, or any future storage kind.
 *
 * <pre>{@code
 * List<LocatedResourceStorage<ItemResource>> found =
 *     ResourceStorageScanner.forResource(ResourceStorageTypes.ITEM_BLOCK.get())
 *         .inBoundaries(roomBounds)
 *         .filter(cornerPositions)
 *         .sides(null, Direction.UP)
 *         .scan(level)
 *         .toList();
 * }</pre>
 *
 * <p>Instances are mutable and not thread-safe; build and consume one per scan.
 */
public final class ResourceStorageScanner<T extends Resource> {

    private static final List<@Nullable Direction> UNSIDED = Collections.singletonList(null);

    private final ResourceStorageType<T> type;

    private @Nullable AABB boundaries;
    private @Nullable Predicate<BlockPos> positionFilter;
    private List<@Nullable Direction> sides = UNSIDED;

    private ResourceStorageScanner(ResourceStorageType<T> type) {
        this.type = type;
    }

    /**
     * Starts a scan for handlers of {@code type}.
     */
    public static <T extends Resource> ResourceStorageScanner<T> forResource(ResourceStorageType<T> type) {
        return new ResourceStorageScanner<>(type);
    }

    public static <T extends Resource> ResourceStorageScanner<T> forResource(Supplier<ResourceStorageType<T>> type) {
        return new ResourceStorageScanner<>(type.get());
    }

    /**
     * Sets the region to scan: every block within {@code boundaries}. Required before {@link #scan}.
     */
    public ResourceStorageScanner<T> filterPositions(AABB boundaries) {
        this.boundaries = boundaries;
        return this;
    }

    /**
     * Narrows the scan to positions contained in {@code positions}. Composes with earlier filters.
     */
    public ResourceStorageScanner<T> filterPositions(Collection<BlockPos> positions) {
        final var allowed = positions.stream().map(BlockPos::immutable).collect(Collectors.toUnmodifiableSet());
        return filterPositions(allowed::contains);
    }

    /**
     * Narrows the scan to positions matching {@code predicate}. Composes (ANDs) with earlier filters.
     */
    public ResourceStorageScanner<T> filterPositions(Predicate<BlockPos> predicate) {
        this.positionFilter = this.positionFilter == null ? predicate : this.positionFilter.and(predicate);
        return this;
    }

    /**
     * Sets the sides to probe at each position; {@code null} means the unsided capability. Defaults to
     * unsided only.
     */
    public ResourceStorageScanner<T> sides(@Nullable Direction... sides) {
        this.sides = Arrays.asList(sides);
        return this;
    }

    /**
     * Runs the scan against {@code level}. A position that exposes the capability on several of the
     * requested {@link #sides(Direction...) sides} yields one {@link LocatedResourceStorage} per
     * matching side; positions with no handler are skipped.
     *
     * @throws IllegalStateException if no region was set via {@link #filterPositions}.
     */
    public Stream<LocatedResourceStorage<T>> scan(ServerLevel level) {
        if (boundaries == null && positionFilter == null)
            throw new IllegalStateException("No scan region set; call filterPositions before scan(level).");

        final var capability = type.blockCapability();

        Stream<BlockPos> positions = Stream.empty();
        if (boundaries != null)
            positions = BlockPos.betweenClosedStream(boundaries).map(BlockPos::immutable);

        if (positionFilter != null)
            positions = positions.filter(positionFilter);

        return positions.flatMap(pos -> sides.stream()
                .map(side -> {
                    final var handler = level.getCapability(capability, pos, side);
                    if (handler == null)
                        return null;

                    final var storage = new ResourceStoragePosition<>(
                            GlobalPos.of(level.dimension(), pos), side, type);
                    return new LocatedResourceStorage<>(storage, handler);
                })
                .filter(Objects::nonNull));
    }
}
