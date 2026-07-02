package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.datagen.base.compat.PSDCuriosProvider;
import dev.compactmods.machines.datagen.base.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.base.lang.FrenchLangGenerator;
import dev.compactmods.machines.datagen.base.loot.BlockLootGenerator;
import dev.compactmods.machines.datagen.base.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.base.tags.ItemTagGenerator;
import dev.compactmods.machines.datagen.base.tags.PointOfInterestTagGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BaseDatapack {

    public record BaseDatapackGenerationResults(CompletableFuture<HolderLookup.Provider> holderLookupProvider) {
        //
    }

    public static BaseDatapackGenerationResults generatePack(GatherDataEvent event) {
        final var fileHelper = event.getExistingFileHelper();
        final var generator = event.getGenerator();

        final var basePackOutput = generator.getPackOutput();
        final var holderLookup = event.getLookupProvider();

        // Server
        boolean server = event.includeServer();

        var dataRegistered = generator.addProvider(server, new DatapackRegisteredStuff(basePackOutput, holderLookup));
        generator.addProvider(server, new LootTableProvider(basePackOutput,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)),
                holderLookup
        ));

        generator.addProvider(server, new RecipeGenerator(basePackOutput, dataRegistered.getRegistryProvider()));

        final var blocks = new BlockTagGenerator(basePackOutput, fileHelper, holderLookup);
        generator.addProvider(server, blocks);
        generator.addProvider(server, new ItemTagGenerator(basePackOutput, blocks, holderLookup));

        // CURIOS Integration
        generator.addProvider(server, new PSDCuriosProvider(basePackOutput, holderLookup, fileHelper));

        generator.addProvider(server, new PointOfInterestTagGenerator(basePackOutput, holderLookup, fileHelper));

        // Client
        boolean client = event.includeClient();
        generator.addProvider(client, new StateGenerator(basePackOutput, fileHelper));
        generator.addProvider(client, new ItemModelGenerator(basePackOutput, fileHelper));

        generator.addProvider(client, new EnglishLangGenerator(basePackOutput));
        generator.addProvider(client, new FrenchLangGenerator(basePackOutput));

        return new BaseDatapackGenerationResults(dataRegistered.getRegistryProvider());
    }
}
