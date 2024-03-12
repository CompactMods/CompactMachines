package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.datagen.compat.curios.CurioEntityGenerator;
import dev.compactmods.machines.datagen.compat.curios.CurioSlotGenerator;
import dev.compactmods.machines.datagen.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.lang.RussianLangGenerator;
import dev.compactmods.machines.datagen.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.tags.ItemTagGenerator;
import dev.compactmods.machines.datagen.tags.PointOfInterestTagGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var fileHelper = event.getExistingFileHelper();
        final var generator = event.getGenerator();

        final var packOut = generator.getPackOutput();
        final var holderLookup = event.getLookupProvider();

        // Server
        boolean server = event.includeServer();
        generator.addProvider(server, new DatapackRegisteredStuff(packOut, holderLookup));
        generator.addProvider(server, (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(output,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK))
        ));

        generator.addProvider(server, new RecipeGenerator(packOut));

        final var blocks = new BlockTagGenerator(packOut, fileHelper, holderLookup);
        generator.addProvider(server, blocks);
        generator.addProvider(server, new ItemTagGenerator(packOut, blocks, holderLookup));

        // CURIOS Integration
        generator.addProvider(server, new CurioSlotGenerator(packOut, holderLookup, fileHelper));
        generator.addProvider(server, new CurioEntityGenerator(packOut, holderLookup, fileHelper));

        generator.addProvider(server, new PointOfInterestTagGenerator(packOut, holderLookup, fileHelper));
        // generator.addProvider(event.includeServer(), new PackMetaGenerator(packOut));

        // Client
        boolean client = event.includeClient();
        generator.addProvider(client, new StateGenerator(packOut, fileHelper));
        // generator.addProvider(client, new TunnelWallStateGenerator(packOut, helper));
        generator.addProvider(client, new ItemModelGenerator(packOut, fileHelper));

        generator.addProvider(client, new EnglishLangGenerator(generator));
        generator.addProvider(client, new RussianLangGenerator(generator));
    }
}
