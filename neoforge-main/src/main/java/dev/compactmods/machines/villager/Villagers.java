package dev.compactmods.machines.villager;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Villagers {
    public static final ResourceLocation TINKERER_ID = CompactMachines.modRL("tinkerer");

    public static final ResourceKey<PoiType> TINKERER_WORKBENCH_KEY = ResourceKey
            .create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), TINKERER_ID);

    public static final DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister
            .create(BuiltInRegistries.VILLAGER_PROFESSION, CompactMachines.MOD_ID);

    public static final DeferredRegister<VillagerTrades.ItemListing> TRADES = DeferredRegister
            .create(CompactMachines.modRL("tinkerer_trades"), CompactMachines.MOD_ID);

    public static final DeferredBlock<Block> SPATIAL_WORKBENCH = CMRegistries.BLOCKS.registerSimpleBlock("spatial_workbench", BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE));

    public static final DeferredItem<BlockItem> SPATIAL_WORKBENCH_ITEM = CMRegistries.ITEMS.registerSimpleBlockItem(SPATIAL_WORKBENCH);

    public static final Holder<VillagerProfession> TINKERER = VILLAGERS.register("tinkerer",
            () -> new VillagerProfession(
                    Component.translatable("entity." + TINKERER_ID.getNamespace() + ".villager." + TINKERER_ID.getPath()),
                    holder -> holder.is(TINKERER_WORKBENCH_KEY), //jobSite
                    holder -> holder.is(TINKERER_WORKBENCH_KEY), //acquirable jobSite
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_TOOLSMITH
            ));

    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> TEST_TRADE = TRADES.register("test",
            () -> new BasicItemListing(1, Machines.Blocks.UNBOUND_MACHINE.toStack(1), 5, 100));

    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> TEST_TRADE2 = TRADES.register("shrinking_device",
            () -> new BasicItemListing(5, Shrinking.PERSONAL_SHRINKING_DEVICE.toStack(1), 1, 300));

    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> SHRINKING = TRADES.register("shrinking_modules",
            () -> new BasicItemListing(3, Shrinking.SHRINKING_MODULE.toStack(4), 5, 100));

    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> ENLARGING = TRADES.register("enlarging_modules",
            () -> new BasicItemListing(3, Shrinking.ENLARGING_MODULE.toStack(4), 5, 100));

    static {
        CMRegistries.POINTS_OF_INTEREST.register("tinkerer", () -> new PoiType(
                ImmutableSet.of(SPATIAL_WORKBENCH.get().defaultBlockState()), 1, 1)
        );
    }

    public static void prepare() {

    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(CMVillagerTrades::addSpatialTinkererTrades);
        NeoForge.EVENT_BUS.addListener(VillageAdditions::injectVillageBuildings);
    }
}
