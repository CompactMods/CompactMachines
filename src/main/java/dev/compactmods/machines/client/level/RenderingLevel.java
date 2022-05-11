package dev.compactmods.machines.client.level;

import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import javax.annotation.Nullable;
import java.util.*;

public class RenderingLevel extends Level {

    private final TemplateChunkProvider chunkProvider;

    public RenderingLevel(StructureTemplate blocks) {
        super(new FakeSpawnInfo(), Level.OVERWORLD, Holder.direct(DimensionType.DEFAULT_OVERWORLD),
                () -> InactiveProfiler.INSTANCE, true, false, 0);

        if(!blocks.palettes.isEmpty()) {
            StructurePlaceSettings s = new StructurePlaceSettings();
            var p = s.getRandomPalette(blocks.palettes, null);
            this.chunkProvider = new TemplateChunkProvider(p.blocks(), this, (po) -> true);
        } else {
            this.chunkProvider = new TemplateChunkProvider(Collections.emptyList(), this, p -> true);
        }
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {

    }

    @Override
    public void playSound(@Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_) {

    }

    @Override
    public void playSound(@Nullable Player p_46551_, Entity p_46552_, SoundEvent p_46553_, SoundSource p_46554_, float p_46555_, float p_46556_) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Nullable
    @Override
    public Entity getEntity(int p_46492_) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String p_46650_) {
        return null;
    }

    @Override
    public void setMapData(String p_151533_, MapItemSavedData p_151534_) {

    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return new RecipeManager();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return new EmptyLevelEntityGetter();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkProvider;
    }

    @Override
    public void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {

    }

    @Override
    public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {

    }

    @Override
    public RegistryAccess registryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }

    @Override
    public float getShade(Direction p_45522_, boolean p_45523_) {
        return 1;
    }

    @Override
    public List<? extends Player> players() {
        return Collections.emptyList();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        return Holder.direct(registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS));
    }

    @Override
    public int getBrightness(LightLayer p_45518_, BlockPos p_45519_) {
        return Level.MAX_BRIGHTNESS;
    }

    @Override
    public long getGameTime() {
        return Minecraft.getInstance().level.getGameTime();
    }

    public void tbe() {
        tickBlockEntities();
    }

    @Override
    protected void tickBlockEntities() {
        super.tickBlockEntities();
        chunkProvider.chunks()
                .filter(ca -> ca instanceof TemplateChunk)
                .map(TemplateChunk.class::cast)
                .forEach(TemplateChunk::tick);
    }
}
