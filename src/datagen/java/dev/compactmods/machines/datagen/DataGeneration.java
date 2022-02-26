package dev.compactmods.machines.datagen;

import java.io.IOException;
import dev.compactmods.machines.CompactMachines;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) throws IOException {
        if (event.includeServer())
            registerServerProviders(event.getGenerator(), event);

        if (event.includeClient())
            registerClientProviders(event.getGenerator(), event);
    }

    private static void registerServerProviders(DataGenerator generator, GatherDataEvent event) throws IOException {
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(new LevelBiomeGenerator(generator, helper));

        generator.addProvider(new BlockLootGenerator(generator));
        generator.addProvider(new RecipeGenerator(generator));
        generator.addProvider(new AdvancementGenerator(generator));
        generator.addProvider(new TagGenerator(generator, helper));
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(new StateGenerator(generator, helper));
        generator.addProvider(new TunnelWallStateGenerator(generator, helper));
        generator.addProvider(new ItemModelGenerator(generator, helper));

    }
}
