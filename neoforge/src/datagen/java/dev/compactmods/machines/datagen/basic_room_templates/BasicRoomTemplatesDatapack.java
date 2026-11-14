package dev.compactmods.machines.datagen.basic_room_templates;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.datagen.basic_room_templates.lang.RoomTemplatesEnglishLangGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

public class BasicRoomTemplatesDatapack {

    private static final String PACK_NAME = "basic_templates";

    public static void generatePack(GatherDataEvent event) {
        final var generator = event.getGenerator();

        DataGenerator.PackGenerator basicTemplates = generator.getBuiltinDatapack(true, CompactMachinesCore.MOD_ID, PACK_NAME);
        basicTemplates.addProvider(output -> PackMetadataGenerator.forFeaturePack(
                output,
                Component.literal("Enables the basic room templates, built in to the mod.")
        ));

        // Same output folder as getBuiltinDatapack, but as a NeoForge PackGenerator so it can emit registry entries
        final var packOutput = new PackOutput(generator.getPackOutput()
                .getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(CompactMachinesCore.MOD_ID)
                .resolve("datapacks")
                .resolve(PACK_NAME));

        final var registries = event.getPackGenerator(packOutput);
        final var modIds = Set.of(CompactMachinesCore.MOD_ID);

        // Templates first, so the recipe bootstrap below can look them up
        registries.createWorldRegistryObjects(RoomTemplatesGenerator.BUILDER, modIds, PACK_NAME + "_world");
        registries.createReloadableRegistryObjects(new RegistrySetBuilder()
                .add(RecipeProvider.asBootstrap(BasicRoomTemplateRecipeGenerator::new)),
                modIds, PACK_NAME + "_reloadable");

        basicTemplates.addProvider(RoomTemplatesEnglishLangGenerator::new);
    }
}
