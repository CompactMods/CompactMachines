package dev.compactmods.machines.room.preview.client;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.RoomBlockEntitySnapshot;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A read-only, client-side level over a {@link RoomPreviewSnapshot}, in world coordinates (the room's
 * real positions, which block entities' sync data often uses to name other blocks), holding
 * the room's block entities rebuilt from their sync tags. It gives those block entities a level to
 * live in, so their model data (pipe connections) and renderers (chest facing and pairing) see the
 * room's real neighbours, as they would in the world.
 *
 * <p>Like Create's Ponder levels, it can't be changed: writes are ignored, it has no entities, its
 * chunks are vanilla's empty placeholders, and it's always fully lit. It is a client
 * {@link BlockAndTintGetter}, as client levels are, with tints from the snapshot's biomes. Its block
 * entities do tick ({@link #tick}), so client-side animation (items moving through pipes) plays between
 * snapshots. A new snapshot {@linkplain #update updates} the level in place: block entities that are still
 * there keep their client state and only get the new sync data, as they would from an update packet in
 * the world, so animation carries on instead of restarting. World-wide services (registries, recipes, data) come from the player's current level.
 * Block entities that fail to load are left out; ones whose ticker fails stop ticking.
 */
public final class PreviewLevel extends Level implements BlockAndTintGetter {

    private RoomPreviewSnapshot snapshot;
    private final ClientLevel host;
    private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
    /** The sync tag each block entity last loaded, so unchanged ones aren't reloaded. */
    private final Map<BlockPos, CompoundTag> appliedTags = new HashMap<>();
    /** Block entities whose ticker threw; they don't tick again until rebuilt. */
    private final Set<BlockPos> brokenTickers = new HashSet<>();
    private final Map<BlockPos, ModelData> modelData = new HashMap<>();
    private final Map<BlockPos, BlockEntityTicker<BlockEntity>> tickers = new HashMap<>();
    private final ChunkSource chunks = new EmptyChunkSource();
    private final Map<Long, LevelChunk> emptyChunks = new HashMap<>();
    private BlockAndTintGetter tints;

    private PreviewLevel(RoomPreviewSnapshot snapshot, ClientLevel host, BlockAndTintGetter tints) {
        super(host.getLevelData(), CompactDimension.LEVEL_KEY, host.registryAccess(),
                host.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).get(CompactDimension.DIM_TYPE_KEY)
                        .<Holder<net.minecraft.world.level.dimension.DimensionType>>map(h -> h)
                        .orElse(host.dimensionTypeRegistration()),
                true, false, 0L, 0);
        this.snapshot = snapshot;
        this.host = host;
        this.tints = tints;
    }

    /**
     * {@return a level over {@code snapshot}, with its block entities loaded}
     *
     * @param latest block entity data newer than the snapshot's, by room-local position
     * @param tints  where block tints come from (the snapshot's biomes)
     */
    public static PreviewLevel create(RoomPreviewSnapshot snapshot, Map<BlockPos, RoomBlockEntitySnapshot> latest,
                                      ClientLevel host, BlockAndTintGetter tints) {
        return new PreviewLevel(snapshot, host, tints).update(snapshot, latest, tints);
    }

    /** {@return whether this level can be {@linkplain #update updated} to {@code snapshot} while the player is in {@code host}} */
    public boolean canUpdate(ClientLevel host, RoomPreviewSnapshot snapshot) {
        return this.host == host && snapshot.origin().equals(origin());
    }

    /** {@return the interior's minimum corner, in the world}: where room-local {@code (0, 0, 0)} is */
    public BlockPos origin() {
        return snapshot.origin();
    }

    /**
     * Moves this level to a newer snapshot of the same room. Block entities that are still there, with
     * the same type, are kept: they get the new block state and, if their sync tag changed, the new tag.
     * Others are dropped or created.
     *
     * @param latest block entity data newer than the snapshot's, by room-local position; used in its place
     * @param tints  where block tints come from (the snapshot's biomes)
     * @return this level
     */
    public PreviewLevel update(RoomPreviewSnapshot snapshot, Map<BlockPos, RoomBlockEntitySnapshot> latest,
                               BlockAndTintGetter tints) {
        this.snapshot = snapshot;
        this.tints = tints;

        final Map<BlockPos, RoomBlockEntitySnapshot> incoming = new HashMap<>();
        for (RoomBlockEntitySnapshot data : snapshot.blockEntities()) {
            final BlockPos pos = origin().offset(data.local());
            final var newer = latest.get(data.local());
            incoming.put(pos, newer != null && newer.type() == data.type() ? newer : data);
        }

        // Drop block entities that are gone, changed type, or no longer fit their block
        for (BlockPos pos : List.copyOf(blockEntities.keySet())) {
            final var data = incoming.get(pos);
            final BlockEntity be = blockEntities.get(pos);
            if (data == null || data.type() != be.getType() || !data.type().isValid(getBlockState(pos)))
                forget(pos);
        }

        incoming.forEach((pos, data) -> {
            final BlockEntity existing = blockEntities.get(pos);
            if (existing == null)
                load(pos, data);
            else
                refresh(pos, existing, data);
        });

        // Model data reads neighbours, so only once every block entity is up to date
        modelData.clear();
        blockEntities.keySet().forEach(this::refreshModelData);
        return this;
    }

    /**
     * Applies newer sync data to block entities already in the level, as an update packet would in the
     * world. Ones not in the level (yet) are skipped; they come with the next snapshot.
     *
     * @return whether any block's model data changed, so the mesh needs rebaking
     */
    public boolean applyBlockEntities(List<RoomBlockEntitySnapshot> changed) {
        final Set<BlockPos> touched = new HashSet<>();
        for (RoomBlockEntitySnapshot data : changed) {
            final BlockPos pos = origin().offset(data.local());
            final BlockEntity be = blockEntities.get(pos);
            if (be == null || be.getType() != data.type())
                continue;

            refresh(pos, be, data);
            // Model data may read neighbours, so theirs may change too
            touched.add(pos);
            for (Direction side : Direction.values())
                touched.add(pos.relative(side));
        }

        boolean remesh = false;
        for (BlockPos pos : touched) {
            if (blockEntities.containsKey(pos))
                remesh |= refreshModelData(pos);
        }
        return remesh;
    }

    /** Recomputes one block entity's model data; {@return whether it changed} */
    private boolean refreshModelData(BlockPos pos) {
        final BlockEntity be = blockEntities.get(pos);
        ModelData data = ModelData.EMPTY;
        if (be != null) {
            try {
                data = be.getModelData();
            } catch (Exception | LinkageError e) {
                CompactMachinesCore.modLog().debug("Room preview: no model data for {} at {}", be.getType(), pos, e);
            }
        }

        final ModelData previous = data == ModelData.EMPTY ? modelData.remove(pos) : modelData.put(pos, data);
        return !sameModelData(previous == null ? ModelData.EMPTY : previous, data);
    }

    /** {@link ModelData} compares by identity; models only care about the values. */
    private static boolean sameModelData(ModelData a, ModelData b) {
        if (a == b)
            return true;
        if (!a.getProperties().equals(b.getProperties()))
            return false;
        for (ModelProperty<?> property : a.getProperties()) {
            if (!Objects.equals(a.get(property), b.get(property)))
                return false;
        }
        return true;
    }

    private void load(BlockPos pos, RoomBlockEntitySnapshot data) {
        final BlockState state = getBlockState(pos);
        if (!data.type().isValid(state))
            return;

        try {
            final BlockEntity be = data.type().create(pos, state);
            if (be == null)
                return;

            // As a client chunk does: placed in the level, then given its sync tag
            be.setLevel(this);
            blockEntities.put(pos, be);
            applyTag(pos, be, data.tag());
            updateTicker(pos, be);
        } catch (Exception | LinkageError e) {
            forget(pos);
            CompactMachinesCore.modLog().debug("Room preview: couldn't load {} at {}", data.type(), pos, e);
        }
    }

    /** Brings a kept block entity up to date, as an update packet would in the world. */
    private void refresh(BlockPos pos, BlockEntity be, RoomBlockEntitySnapshot data) {
        try {
            final BlockState state = getBlockState(pos);
            if (be.getBlockState() != state) {
                be.setBlockState(state);
                updateTicker(pos, be);
            }

            if (!data.tag().equals(appliedTags.get(pos)))
                applyTag(pos, be, data.tag());
        } catch (Exception | LinkageError e) {
            forget(pos);
            CompactMachinesCore.modLog().debug("Room preview: couldn't update {} at {}", data.type(), pos, e);
        }
    }

    private void applyTag(BlockPos pos, BlockEntity be, CompoundTag tag) {
        be.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, registryAccess(), tag));
        appliedTags.put(pos, tag);
    }

    @SuppressWarnings("unchecked")
    private void updateTicker(BlockPos pos, BlockEntity be) {
        tickers.remove(pos);
        if (brokenTickers.contains(pos))
            return;

        final var type = (BlockEntityType<BlockEntity>) be.getType();
        final BlockEntityTicker<BlockEntity> ticker = be.getBlockState().getTicker(this, type);
        if (ticker != null)
            tickers.put(pos, ticker);
    }

    private void forget(BlockPos pos) {
        final BlockEntity be = blockEntities.remove(pos);
        if (be != null) {
            try {
                be.setRemoved();
            } catch (Exception | LinkageError e) {
                CompactMachinesCore.modLog().debug("Room preview: removing {} at {} failed", be.getType(), pos, e);
            }
        }
        appliedTags.remove(pos);
        tickers.remove(pos);
        brokenTickers.remove(pos);
        modelData.remove(pos);
    }

    /** Runs the block entities' client tickers once. Call every client tick while the preview is shown. */
    public void tick() {
        final var it = tickers.entrySet().iterator();
        while (it.hasNext()) {
            final var entry = it.next();
            final BlockEntity be = blockEntities.get(entry.getKey());
            try {
                entry.getValue().tick(this, entry.getKey(), be.getBlockState(), be);
            } catch (Exception | LinkageError e) {
                it.remove();
                brokenTickers.add(entry.getKey());
                CompactMachinesCore.modLog().debug("Room preview: {} at {} stopped ticking", be.getType(), entry.getKey(), e);
            }
        }
    }

    /** {@return the room's block entities}, at their world positions */
    public Collection<BlockEntity> blockEntities() {
        return Collections.unmodifiableCollection(blockEntities.values());
    }

    // --- reads ------------------------------------------------------------------------

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return snapshot.blockAtWorld(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return blockEntities.get(pos);
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return modelData.getOrDefault(pos, ModelData.EMPTY);
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return true;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        return tints.getBlockTint(pos, color);
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int darkening) {
        return 15;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getSeaLevel() {
        return getMinY();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
        return registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(CompactDimension.COMPACT_BIOME_KEY);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return host.getWorldBorder();
    }

    // --- writes are ignored -----------------------------------------------------------

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, @Block.UpdateFlags int flags, int recursionLeft) {
        return false;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
    }

    /** Block entities call this when their data changes; there's nothing to save. */
    @Override
    public void blockEntityChanged(BlockPos pos) {
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState old, BlockState current, @Block.UpdateFlags int updateFlags) {
    }

    @Override
    public void playSeededSound(@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void playSeededSound(@Nullable Entity except, Entity sourceEntity, Holder<SoundEvent> sound,
                                SoundSource source, float volume, float pitch, long seed) {
    }

    @Override
    public void levelEvent(@Nullable Entity source, int type, BlockPos pos, int data) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 position, GameEvent.Context context) {
    }

    @Override
    public void destroyBlockProgress(int id, BlockPos blockPos, int progress) {
    }

    // --- no entities, ticks or chunks -------------------------------------------------

    @Override
    public @Nullable Entity getEntity(int id) {
        return null;
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Collection<? extends net.neoforged.neoforge.entity.PartEntity<?>> dragonParts() {
        return List.of();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return EmptyEntityGetter.INSTANCE;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<net.minecraft.world.level.material.Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunks;
    }

    @Override
    public String gatherChunkSourceStats() {
        return "room preview";
    }

    // --- world-wide services, from the player's level ---------------------------------

    @Override
    public TickRateManager tickRateManager() {
        return host.tickRateManager();
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId id) {
        return null;
    }

    @Override
    public Scoreboard getScoreboard() {
        return host.getScoreboard();
    }

    @Override
    public RecipeAccess recipeAccess() {
        return host.recipeAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return host.enabledFeatures();
    }

    @Override
    public ClockManager clockManager() {
        return host.clockManager();
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        return host.environmentAttributes();
    }

    @Override
    public void setRespawnData(LevelData.RespawnData respawnData) {
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        return host.getRespawnData();
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource,
                        @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r,
                        boolean fire, Level.ExplosionInteraction interactionType, ParticleOptions smallExplosionParticles,
                        ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> secondaryParticles,
                        Holder<SoundEvent> explosionSound) {
    }

    /**
     * Vanilla's empty placeholder chunks, as a client level uses for chunks it doesn't have, for code
     * that asks for a chunk. The level's own reads (blocks, block entities) don't go through them.
     */
    private final class EmptyChunkSource extends ChunkSource {
        @Override
        public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean loadOrGenerate) {
            return emptyChunks.computeIfAbsent(ChunkPos.pack(x, z), _ ->
                    new EmptyLevelChunk(PreviewLevel.this, new ChunkPos(x, z), getUncachedNoiseBiome(0, 0, 0)));
        }

        @Override
        public boolean hasChunk(int x, int z) {
            return true;
        }

        @Override
        public void tick(java.util.function.BooleanSupplier haveTime, boolean tickChunks) {
        }

        @Override
        public String gatherStats() {
            return "room preview";
        }

        @Override
        public int getLoadedChunksCount() {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return LevelLightEngine.EMPTY;
        }

        @Override
        public Level getLevel() {
            return PreviewLevel.this;
        }
    }

    /** No entities. */
    private enum EmptyEntityGetter implements LevelEntityGetter<Entity> {
        INSTANCE;

        @Override
        public @Nullable Entity get(int id) {
            return null;
        }

        @Override
        public @Nullable Entity get(UUID id) {
            return null;
        }

        @Override
        public Iterable<Entity> getAll() {
            return List.of();
        }

        @Override
        public <U extends Entity> void get(EntityTypeTest<Entity, U> type, AbortableIterationConsumer<U> consumer) {
        }

        @Override
        public void get(AABB bb, Consumer<Entity> output) {
        }

        @Override
        public <U extends Entity> void get(EntityTypeTest<Entity, U> type, AABB bb, AbortableIterationConsumer<U> consumer) {
        }
    }
}
