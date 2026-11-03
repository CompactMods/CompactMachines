package dev.compactmods.machines.room.preview;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An immutable snapshot of a room interior's block geometry, in a form cheap to ship to clients and
 * cheap to turn into a preview mesh.
 *
 * <p>The interior is captured as a dense {@code sizeX × sizeY × sizeZ} grid of {@link BlockState}s
 * stored via a <em>palette</em> — the distinct states are listed once ({@link #palette}) and every
 * cell holds a small integer index into that list ({@link #indices}). Palette index {@code 0} is
 * always {@link Blocks#AIR air}, so the (dominant) empty space costs one palette slot and packs to
 * the minimum bit width. Cells are ordered {@code index = (y * sizeZ + z) * sizeX + x} in room-local
 * coordinates (origin at the interior's minimum corner).
 *
 * <p>The wire form ({@link #STREAM_CODEC}) writes the dimensions, the palette (each state by its
 * global block-state id), and the index grid bit-packed to {@code ceil(log2(paletteSize))} bits per
 * cell — the same scheme vanilla uses for chunk sections. A 15³ room of mostly air is a 1-bit grid
 * plus a two-entry palette: a few hundred bytes rather than tens of kilobytes.
 *
 * <p>{@link #biomes} carries the interior's biomes (see {@link RoomPreviewBiomes}) so the client can
 * biome-tint the preview.
 *
 * <p>{@link #origin} is the interior's minimum corner in the world, so the client can place block
 * entities at their real positions: mods' sync data often names other blocks by world position.
 *
 * <p>{@link #blockEntities} carries each block entity's sync tag (see {@link RoomBlockEntitySnapshot}),
 * for models that read block entity data and for block entity renderers.
 *
 * <p>{@link #capture(ServerLevel, AABB)} runs on the server thread (it reads the level); turning a
 * snapshot into a mesh is pure data and may run off-thread on the client.
 */
public record RoomPreviewSnapshot(BlockPos origin, int sizeX, int sizeY, int sizeZ, List<BlockState> palette, int[] indices,
                                  RoomPreviewBiomes biomes, List<RoomBlockEntitySnapshot> blockEntities) {

    /** A room that isn't loaded / has no interior yields this; renderers should skip it. */
    public static final RoomPreviewSnapshot EMPTY =
            new RoomPreviewSnapshot(BlockPos.ZERO, 0, 0, 0, List.of(Blocks.AIR.defaultBlockState()), new int[0], RoomPreviewBiomes.EMPTY, List.of());

    /** Single-block-state codec: encodes a {@link BlockState} as its global block-state registry id. */
    public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC =
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

    private static final StreamCodec<ByteBuf, List<BlockState>> PALETTE_STREAM_CODEC =
            BLOCK_STATE_STREAM_CODEC.apply(ByteBufCodecs.list());

    private static final StreamCodec<ByteBuf, List<RoomBlockEntitySnapshot>> BLOCK_ENTITIES_STREAM_CODEC =
            RoomBlockEntitySnapshot.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, RoomPreviewSnapshot> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RoomPreviewSnapshot decode(ByteBuf buf) {
                    final BlockPos origin = BlockPos.STREAM_CODEC.decode(buf);
                    final int sizeX = ByteBufCodecs.VAR_INT.decode(buf);
                    final int sizeY = ByteBufCodecs.VAR_INT.decode(buf);
                    final int sizeZ = ByteBufCodecs.VAR_INT.decode(buf);
                    final List<BlockState> palette = PALETTE_STREAM_CODEC.decode(buf);

                    final int cellCount = Math.multiplyExact(Math.multiplyExact(sizeX, sizeY), sizeZ);
                    final int[] indices = unpack(buf, cellCount, bitsFor(palette.size()));
                    final RoomPreviewBiomes biomes = RoomPreviewBiomes.STREAM_CODEC.decode(buf);
                    final List<RoomBlockEntitySnapshot> blockEntities = BLOCK_ENTITIES_STREAM_CODEC.decode(buf);
                    return new RoomPreviewSnapshot(origin, sizeX, sizeY, sizeZ, palette, indices, biomes, blockEntities);
                }

                @Override
                public void encode(ByteBuf buf, RoomPreviewSnapshot snapshot) {
                    BlockPos.STREAM_CODEC.encode(buf, snapshot.origin);
                    ByteBufCodecs.VAR_INT.encode(buf, snapshot.sizeX);
                    ByteBufCodecs.VAR_INT.encode(buf, snapshot.sizeY);
                    ByteBufCodecs.VAR_INT.encode(buf, snapshot.sizeZ);
                    PALETTE_STREAM_CODEC.encode(buf, snapshot.palette);
                    pack(buf, snapshot.indices, bitsFor(snapshot.palette.size()));
                    RoomPreviewBiomes.STREAM_CODEC.encode(buf, snapshot.biomes);
                    BLOCK_ENTITIES_STREAM_CODEC.encode(buf, snapshot.blockEntities);
                }
            };

    /**
     * Captures the interior of {@code innerBounds} in {@code level} as a snapshot.
     *
     * <p>Only call this once the region's chunks are loaded (see the gatherer's load gate): it reads
     * block states directly and must not force-load chunks. The bounds are the room's
     * {@link dev.compactmods.machines.api.room.spatial.RoomBoundaries#innerBounds() inner bounds} — the
     * air interior, walls excluded.
     */
    public static RoomPreviewSnapshot capture(ServerLevel level, AABB innerBounds) {
        final int minX = Mth.floor(innerBounds.minX);
        final int minY = Mth.floor(innerBounds.minY);
        final int minZ = Mth.floor(innerBounds.minZ);
        final int sizeX = Mth.ceil(innerBounds.maxX) - minX;
        final int sizeY = Mth.ceil(innerBounds.maxY) - minY;
        final int sizeZ = Mth.ceil(innerBounds.maxZ) - minZ;

        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0)
            return EMPTY;

        final List<BlockState> palette = new ArrayList<>();
        final Object2IntMap<BlockState> lookup = new Object2IntOpenHashMap<>();
        lookup.defaultReturnValue(-1);
        // Reserve palette slot 0 for air so empty cells cost the fewest bits.
        palette.add(Blocks.AIR.defaultBlockState());
        lookup.put(Blocks.AIR.defaultBlockState(), 0);

        final int[] indices = new int[sizeX * sizeY * sizeZ];
        final List<RoomBlockEntitySnapshot> blockEntities = new ArrayList<>();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    final BlockState state = level.getBlockState(cursor.set(minX + x, minY + y, minZ + z));
                    int id = lookup.getInt(state);
                    if (id == -1) {
                        id = palette.size();
                        palette.add(state);
                        lookup.put(state, id);
                    }
                    indices[cellIndex(x, y, z, sizeX, sizeZ)] = id;

                    if (state.hasBlockEntity()) {
                        final BlockEntity be = level.getBlockEntity(cursor);
                        if (be != null)
                            blockEntities.add(new RoomBlockEntitySnapshot(new BlockPos(x, y, z), be.getType(),
                                    be.getUpdateTag(level.registryAccess())));
                    }
                }
            }
        }

        final RoomPreviewBiomes biomes = RoomPreviewBiomes.capture(level, minX, minY, minZ, sizeX, sizeY, sizeZ);
        return new RoomPreviewSnapshot(new BlockPos(minX, minY, minZ), sizeX, sizeY, sizeZ, palette, indices, biomes,
                List.copyOf(blockEntities));
    }

    /** {@return the block-local cell index for local coordinates in a grid of the given dimensions} */
    public static int cellIndex(int x, int y, int z, int sizeX, int sizeZ) {
        return (y * sizeZ + z) * sizeX + x;
    }

    /** {@return the block state at room-local coordinates}; out-of-range coordinates return air. */
    public BlockState blockAt(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ)
            return palette.get(0);
        return palette.get(indices[cellIndex(x, y, z, sizeX, sizeZ)]);
    }

    /** {@return the block state at a world position}; outside the interior is air. */
    public BlockState blockAtWorld(BlockPos pos) {
        return blockAt(pos.getX() - origin.getX(), pos.getY() - origin.getY(), pos.getZ() - origin.getZ());
    }

    /** {@return true if this snapshot has no volume or only contains air} */
    public boolean isEmpty() {
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0)
            return true;
        for (int index : indices)
            if (index != 0)
                return false;
        return true;
    }

    /**
     * {@return a cheap content hash} Used by the server to detect whether a room actually changed
     * between scans, so unchanged rooms are not re-sent. Two snapshots with equal geometry hash equal.
     * Block entities count by position and type only: their changing data goes out every tick in
     * {@code RoomBlockEntitiesPacket}s, and mustn't make the whole room resend.
     */
    public int contentHash() {
        int h = origin.hashCode();
        h = 31 * h + sizeX;
        h = 31 * h + sizeY;
        h = 31 * h + sizeZ;
        h = 31 * h + palette.hashCode();
        h = 31 * h + Arrays.hashCode(indices);
        h = 31 * h + biomes.contentHash();
        for (RoomBlockEntitySnapshot be : blockEntities)
            h = 31 * (31 * h + be.local().hashCode()) + be.type().hashCode();
        return h;
    }

    // --- bit-packing -----------------------------------------------------------------

    /** Bits needed to hold any index into a palette of {@code paletteSize} entries (minimum 1). */
    static int bitsFor(int paletteSize) {
        if (paletteSize <= 1)
            return 0;
        return Math.max(1, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
    }

    /** Writes {@code count}-derived index data as a length-prefixed packed {@code long[]}. */
    static void pack(ByteBuf buf, int[] indices, int bits) {
        if (bits == 0) {
            // Palette has a single entry — the grid is entirely that state, no per-cell data needed.
            ByteBufCodecs.VAR_INT.encode(buf, 0);
            return;
        }

        final long mask = (1L << bits) - 1L;
        final long totalBits = (long) indices.length * bits;
        final int longCount = (int) ((totalBits + 63L) / 64L);
        final long[] data = new long[longCount];

        for (int i = 0; i < indices.length; i++) {
            final long bitPos = (long) i * bits;
            final int wordIndex = (int) (bitPos >>> 6);
            final int offset = (int) (bitPos & 63L);
            final long value = indices[i] & mask;

            data[wordIndex] |= value << offset;
            final int spill = offset + bits - 64;
            if (spill > 0)
                data[wordIndex + 1] |= value >>> (bits - spill);
        }

        ByteBufCodecs.VAR_INT.encode(buf, longCount);
        for (long word : data)
            buf.writeLong(word);
    }

    /** Reads {@code count} indices from a length-prefixed packed {@code long[]}. */
    static int[] unpack(ByteBuf buf, int count, int bits) {
        final int longCount = ByteBufCodecs.VAR_INT.decode(buf);
        final long[] data = new long[longCount];
        for (int i = 0; i < longCount; i++)
            data[i] = buf.readLong();

        final int[] indices = new int[count];
        if (bits == 0)
            return indices; // all cells are palette entry 0

        final long mask = (1L << bits) - 1L;
        for (int i = 0; i < count; i++) {
            final long bitPos = (long) i * bits;
            final int wordIndex = (int) (bitPos >>> 6);
            final int offset = (int) (bitPos & 63L);

            long value = data[wordIndex] >>> offset;
            final int spill = offset + bits - 64;
            if (spill > 0)
                value |= data[wordIndex + 1] << (bits - spill);

            indices[i] = (int) (value & mask);
        }
        return indices;
    }

    // Records don't get useful equals/hashCode for the int[] field; override so snapshot equality
    // (used in tests and caches) compares grid contents rather than array identity.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomPreviewSnapshot other)) return false;
        return origin.equals(other.origin) && sizeX == other.sizeX && sizeY == other.sizeY && sizeZ == other.sizeZ
                && palette.equals(other.palette) && Arrays.equals(indices, other.indices)
                && biomes.equals(other.biomes) && blockEntities.equals(other.blockEntities);
    }

    @Override
    public int hashCode() {
        return contentHash();
    }

    @Override
    public String toString() {
        return "RoomPreviewSnapshot[" + sizeX + "x" + sizeY + "x" + sizeZ
                + ", palette=" + palette.size() + ", blockEntities=" + blockEntities.size() + "]";
    }
}
