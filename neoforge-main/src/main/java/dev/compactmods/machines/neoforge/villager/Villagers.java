package dev.compactmods.machines.neoforge.villager;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.function.Supplier;

public class Villagers {
    public static final ResourceLocation TINKERER_ID = new ResourceLocation(Constants.MOD_ID, "tinkerer");

    public static final ResourceKey<PoiType> TINKERER_WORKBENCH_KEY = ResourceKey
            .create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), TINKERER_ID);

    public static final Supplier<VillagerProfession> TINKERER = Registries.VILLAGERS.register("tinkerer",
            () -> new VillagerProfession(
                    TINKERER_ID.toString(),
                    holder -> holder.is(TINKERER_WORKBENCH_KEY), //jobSite
                    holder -> holder.is(TINKERER_WORKBENCH_KEY), //acquirable jobSite
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_TOOLSMITH
            ));

//    public static final DeferredRegister<VillagerTrades.ItemListing> TRADES = DeferredRegister.create(
//            new ResourceLocation(Constants.MOD_ID, "tinkerer_trades"),
//            Constants.MOD_ID
//    );

//    public static final Supplier<IForgeRegistry<VillagerTrades.ItemListing>> TRADES_REG = TRADES
//            .makeRegistry(RegistryBuilder::new);

//    public static final RegistryObject<VillagerTrades.ItemListing> TEST_TRADE = TRADES.register("test",
//            () -> new BasicItemListing(1, new ItemStack(Machines.MACHINE_BLOCK_ITEM_TINY.get()), 5, 100));

    static {
//        Registries.POINTS_OF_INTEREST.register("tinkerer", () -> new PoiType(
//                ImmutableSet.of(MachineRoomUpgrades.WORKBENCH_BLOCK.get().defaultBlockState()), 1, 1)
//        );
    }

    public static void prepare() {

    }
}
