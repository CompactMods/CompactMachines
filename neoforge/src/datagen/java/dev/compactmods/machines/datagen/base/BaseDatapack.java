package dev.compactmods.machines.datagen.base;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.datagen.base.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.base.loot.BlockLootGenerator;
import dev.compactmods.machines.datagen.base.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.base.tags.ItemTagGenerator;
import dev.compactmods.machines.datagen.base.tags.PointOfInterestTagGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeProvider;
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
        // Server
        event.createProvider(ModelAndStateGenerator::new);

        event.createWorldRegistryObjects(DatapackRegisteredStuff.BUILDER, Set.of(CompactMachinesCore.MOD_ID));

        event.createProvider(PointOfInterestTagGenerator::new);

        // Loot tables and recipes are reloadable registries now
        event.createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(Registries.LOOT_TABLE, new LootTableProvider(
                        Collections.emptySet(),
                        List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK))))
                .add(RecipeProvider.asBootstrap(RecipeGenerator::new)),
                Set.of(CompactMachinesCore.MOD_ID));

        event.createProvider(BlockTagGenerator::new);
        event.createProvider(ItemTagGenerator::new);

        // CURIOS Integration
//        generator.addProvider(server, new PSDCuriosProvider(basePackOutput, holderLookup, fileHelper));

//        generator.addProvider(server, new PointOfInterestTagGenerator(basePackOutput, holderLookup, fileHelper));

        // Client
        event.createProvider(EnglishLangGenerator::new);
        event.createProvider(NodeAppearanceGenerator::new);

        return new BaseDatapackGenerationResults(event.getReloadableLookupProvider());
    }
}
