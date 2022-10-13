package dev.compactmods.machines.datagen;

import dev.compactmods.machines.datagen.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.lang.RussianLangGenerator;
import dev.compactmods.machines.datagen.room.RoomTemplates;
import dev.compactmods.machines.datagen.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.tags.ItemTagGenerator;
import dev.compactmods.machines.api.core.Constants;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var helper = event.getExistingFileHelper();
        final var generator = event.getGenerator();

        // Server
        boolean server = event.includeServer();
        generator.addProvider(server, new LevelBiomeGenerator(generator));
        generator.addProvider(server, new BlockLootGenerator(generator));
        generator.addProvider(server, new RecipeGenerator(generator));
        generator.addProvider(server, new AdvancementGenerator(generator));

        final var blocks = new BlockTagGenerator(generator, helper);
        generator.addProvider(server, blocks);
        generator.addProvider(server, new ItemTagGenerator(generator, blocks, helper));

        RoomTemplates.make(event);

        // Client
        boolean client = event.includeClient();
        generator.addProvider(client, new StateGenerator(generator, helper));
        generator.addProvider(client, new TunnelWallStateGenerator(generator, helper));
        generator.addProvider(client, new ItemModelGenerator(generator, helper));

        generator.addProvider(client, new EnglishLangGenerator(generator));
        generator.addProvider(client, new RussianLangGenerator(generator));
    }
}
