//package dev.compactmods.machines.neoforge.client.level;
//
//import dev.compactmods.machines.client.level.EmptyLevelEntityGetter;
//import dev.compactmods.machines.client.level.FakeSpawnInfo;
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.core.Holder;
//import net.minecraft.core.Registry;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.sounds.SoundEvent;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.util.profiling.InactiveProfiler;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.crafting.RecipeManager;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LightLayer;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.biome.Biomes;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.chunk.ChunkSource;
//import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
//import net.minecraft.world.level.entity.LevelEntityGetter;
//import net.minecraft.world.level.gameevent.GameEvent;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
//import net.minecraft.world.phys.Vec3;
//import net.minecraft.world.scores.Scoreboard;
//import net.minecraft.world.ticks.BlackholeTickAccess;
//import net.minecraft.world.ticks.LevelTickAccess;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Collections;
//import java.util.List;
//
//public class RenderingLevel extends Level {
//
//    private final TemplateChunkProvider chunkProvider;
//
//    public RenderingLevel(StructureTemplate blocks) {
//        super(new FakeSpawnInfo(), Level.OVERWORLD, BuiltinRegistries.DIMENSION_TYPE.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
//                () -> InactiveProfiler.INSTANCE, true, false, 0, 1000000);
//
//        if(!blocks.palettes.isEmpty()) {
//            StructurePlaceSettings s = new StructurePlaceSettings();
//            var p = s.getRandomPalette(blocks.palettes, null);
//            this.chunkProvider = new TemplateChunkProvider(p.blocks(), this, (po) -> true);
//        } else {
//            this.chunkProvider = new TemplateChunkProvider(Collections.emptyList(), this, p -> true);
//        }
//    }
//
//    @Override
//    public boolean isClientSide() {
//        return true;
//    }
//
//    @Override
//    public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {
//
//    }
//
//    @Override
//    public void playSeededSound(@Nullable Player p_220363_, double p_220364_, double p_220365_, double p_220366_, SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {
//
//    }
//
//    @Override
//    public void playSeededSound(@Nullable Player p_220372_, Entity p_220373_, SoundEvent p_220374_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {
//
//    }
//
//    @Override
//    public void playSound(@Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_) {
//
//    }
//
//    @Override
//    public void playSound(@Nullable Player p_46551_, Entity p_46552_, SoundEvent p_46553_, SoundSource p_46554_, float p_46555_, float p_46556_) {
//
//    }
//
//    @Override
//    public String gatherChunkSourceStats() {
//        return "";
//    }
//
//    @Nullable
//    @Override
//    public Entity getEntity(int p_46492_) {
//        return null;
//    }
//
//    @Nullable
//    @Override
//    public MapItemSavedData getMapData(String p_46650_) {
//        return null;
//    }
//
//    @Override
//    public void setMapData(String p_151533_, MapItemSavedData p_151534_) {
//
//    }
//
//    @Override
//    public int getFreeMapId() {
//        return 0;
//    }
//
//    @Override
//    public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_) {
//
//    }
//
//    @Override
//    public Scoreboard getScoreboard() {
//        return new Scoreboard();
//    }
//
//    @Override
//    public RecipeManager getRecipeManager() {
//        return new RecipeManager();
//    }
//
//    @Override
//    protected LevelEntityGetter<Entity> getEntities() {
//        return new EmptyLevelEntityGetter();
//    }
//
//    @Override
//    public LevelTickAccess<Block> getBlockTicks() {
//        return BlackholeTickAccess.emptyLevelList();
//    }
//
//    @Override
//    public LevelTickAccess<Fluid> getFluidTicks() {
//        return BlackholeTickAccess.emptyLevelList();
//    }
//
//    @Override
//    public ChunkSource getChunkSource() {
//        return chunkProvider;
//    }
//
//    @Override
//    public void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {
//
//    }
//
//    @Override
//    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_) {
//
//    }
//
//    @Override
//    public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {
//
//    }
//
//    @Override
//    public RegistryAccess registryAccess() {
//        return Minecraft.getInstance().level.registryAccess();
//    }
//
//    @Override
//    public float getShade(Direction p_45522_, boolean p_45523_) {
//        return 1;
//    }
//
//    @Override
//    public List<? extends Player> players() {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
//        return Holder.direct(registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS));
//    }
//
//    @Override
//    public int getBrightness(LightLayer p_45518_, BlockPos p_45519_) {
//        return Level.MAX_BRIGHTNESS;
//    }
//
//    @Override
//    public long getGameTime() {
//        return Minecraft.getInstance().level.getGameTime();
//    }
//
//    public void tbe() {
//        tickBlockEntities();
//    }
//
//    @Override
//    protected void tickBlockEntities() {
//        super.tickBlockEntities();
//        chunkProvider.chunks()
//                .filter(ca -> ca instanceof TemplateChunk)
//                .map(TemplateChunk.class::cast)
//                .toList()
//                .forEach(TemplateChunk::tick);
//    }
//}
