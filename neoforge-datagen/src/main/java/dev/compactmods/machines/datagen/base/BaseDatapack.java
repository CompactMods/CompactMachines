package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.datagen.base.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.base.loot.BlockLootGenerator;
import dev.compactmods.machines.datagen.base.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.base.tags.ItemTagGenerator;
import dev.compactmods.machines.datagen.base.tags.PointOfInterestTagGenerator;
import dev.compactmods.machines.datagen.basic_room_templates.BasicRoomTemplateRecipeGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BaseDatapack {

    public record BaseDatapackGenerationResults(CompletableFuture<HolderLookup.Provider> holderLookupProvider) {
        //
    }

    public static BaseDatapackGenerationResults generatePack(GatherDataEvent.Client event) {
        final var generator = event.getGenerator();

        final var basePackOutput = generator.getPackOutput();
        final var holderLookup = event.getLookupProvider();

        // Server
        event.createProvider(ModelAndStateGenerator::new);

        event.createDatapackRegistryObjects(DatapackRegisteredStuff.BUILDER, Set.of(CompactMachines.MOD_ID));

        event.createProvider(PointOfInterestTagGenerator::new);

        event.addProvider(new LootTableProvider(basePackOutput,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK)),
                holderLookup
        ));

        event.createProvider((output,provider)
                -> new BasicRoomTemplateRecipeGenerator.Runner(CompactMachines.dotPrefix("base"), output, provider));

        event.createProvider(BlockTagGenerator::new);
        event.createProvider(ItemTagGenerator::new);

        // CURIOS Integration
//        generator.addProvider(server, new PSDCuriosProvider(basePackOutput, holderLookup, fileHelper));

//        generator.addProvider(server, new PointOfInterestTagGenerator(basePackOutput, holderLookup, fileHelper));

        // Client
        event.createProvider(EnglishLangGenerator::new);

        return new BaseDatapackGenerationResults(holderLookup);
    }
}
