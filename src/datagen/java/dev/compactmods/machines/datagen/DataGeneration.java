package dev.compactmods.machines.datagen;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.datagen.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.lang.RussianLangGenerator;
import dev.compactmods.machines.datagen.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.tags.ItemTagGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var helper = event.getExistingFileHelper();
        final var generator = event.getGenerator();
        final var packOut = generator.getPackOutput();
        final var holderLookup = event.getLookupProvider();

        // Server
        generator.addProvider(event.includeServer(), new DatapackRegisteredStuff(packOut, holderLookup));
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packOut,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK))
        ));
        generator.addProvider(event.includeServer(), new RecipeGenerator(packOut));

        final var blocks = new BlockTagGenerator(packOut, helper, holderLookup);
        generator.addProvider(event.includeServer(), blocks);
        generator.addProvider(event.includeServer(), new ItemTagGenerator(packOut, blocks, holderLookup));

        // Client
        generator.addProvider(event.includeClient(), new StateGenerator(packOut, helper));
        //generator.addProvider(event.includeClient(), new TunnelWallStateGenerator(packOut, helper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(packOut, helper));

        generator.addProvider(event.includeClient(), new EnglishLangGenerator(generator));
        generator.addProvider(event.includeClient(), new RussianLangGenerator(generator));
    }
}
