package dev.compactmods.machines.datagen;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.datagen.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.lang.RussianLangGenerator;
import dev.compactmods.machines.datagen.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.tags.ItemTagGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var helper = event.getExistingFileHelper();
        final var generator = event.getGenerator();

        // Server
        generator.addProvider(event.includeServer(), new LevelBiomeGenerator(generator));
        generator.addProvider(event.includeServer(), new BlockLootGenerator(generator));
        generator.addProvider(event.includeServer(), new RecipeGenerator(generator));
        generator.addProvider(event.includeServer(), new AdvancementGenerator(generator));

        final var blocks = new BlockTagGenerator(generator, helper);
        generator.addProvider(event.includeServer(), blocks);
        generator.addProvider(event.includeServer(), new ItemTagGenerator(generator, blocks, helper));

        // Client
        generator.addProvider(event.includeClient(), new StateGenerator(generator, helper));
        generator.addProvider(event.includeClient(), new TunnelWallStateGenerator(generator, helper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(generator, helper));

        generator.addProvider(event.includeClient(), new EnglishLangGenerator(generator));
        generator.addProvider(event.includeClient(), new RussianLangGenerator(generator));
    }
}
