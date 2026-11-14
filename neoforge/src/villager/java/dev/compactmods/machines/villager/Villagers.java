package dev.compactmods.machines.villager;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.core.CompactMachinesCore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Villagers {
    public static final Identifier TINKERER_ID = CompactMachinesCore.identifier("tinkerer");

    public static final ResourceKey<PoiType> TINKERER_WORKBENCH_KEY = ResourceKey
            .create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), TINKERER_ID);

    public static final DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister
            .create(BuiltInRegistries.VILLAGER_PROFESSION, CompactMachinesCore.MOD_ID);

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachinesCore.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);
    private static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, CompactMachinesCore.MOD_ID);

    public static final DeferredBlock<Block> SPATIAL_WORKBENCH = BLOCKS.registerSimpleBlock("spatial_workbench", () -> BlockBehaviour
            .Properties.of()
            .mapColor(MapColor.NONE));

    public static final DeferredItem<BlockItem> SPATIAL_WORKBENCH_ITEM = ITEMS.registerSimpleBlockItem(SPATIAL_WORKBENCH);

    public static final Holder<VillagerProfession> TINKERER = VILLAGERS.register("tinkerer",
            () -> new VillagerProfession(
                    Component.translatable("entity." + TINKERER_ID.getNamespace() + ".villager." + TINKERER_ID.getPath()),
                    holder -> holder.is(TINKERER_WORKBENCH_KEY), //jobSite
                    holder -> holder.is(TINKERER_WORKBENCH_KEY), //acquirable jobSite
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_TOOLSMITH,
                    Int2ObjectMap.ofEntries(
                        Int2ObjectMap.entry(1, ResourceKey.create(Registries.TRADE_SET, CompactMachinesCore.identifier("tinkerer")))
                    )
            ));

    // TODO 26.1 - Tinkerer Trades
//    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> TEST_TRADE = TRADES.register("test",
//            () -> new BasicItemListing(25, Machines.Blocks.MACHINE.toStack(1), 5, 100));
//
//    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> TEST_TRADE2 = TRADES.register("shrinking_device",
//            () -> new BasicItemListing(5, Shrinking.PERSONAL_SHRINKING_DEVICE.toStack(1), 1, 300));
//
//    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> SHRINKING = TRADES.register("shrinking_modules",
//            () -> new BasicItemListing(3, Shrinking.SHRINKING_MODULE.toStack(4), 5, 100));
//
//    public static final DeferredHolder<VillagerTrades.ItemListing, BasicItemListing> ENLARGING = TRADES.register("enlarging_modules",
//            () -> new BasicItemListing(3, Shrinking.ENLARGING_MODULE.toStack(4), 5, 100));

    static {
        POINTS_OF_INTEREST.register("tinkerer", () -> new PoiType(
                ImmutableSet.of(SPATIAL_WORKBENCH.get().defaultBlockState()), 1, 1)
        );
    }

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        POINTS_OF_INTEREST.register(modBus);
        VILLAGERS.register(modBus);
        registerEvents();
    }

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(VillageAdditions::injectVillageBuildings);
    }
}
