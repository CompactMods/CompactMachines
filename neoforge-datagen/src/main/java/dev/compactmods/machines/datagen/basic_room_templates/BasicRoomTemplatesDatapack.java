package dev.compactmods.machines.datagen.basic_room_templates;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.datagen.basic_room_templates.lang.RoomTemplatesEnglishLangGenerator;
import dev.compactmods.machines.datagen.basic_room_templates.lang.RoomTemplatesFrenchLangGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class BasicRoomTemplatesDatapack {

    public static void generatePack(GatherDataEvent event, CompletableFuture<HolderLookup.Provider> registryProvider) {
        final var generator = event.getGenerator();

        DataGenerator.PackGenerator basicTemplates = generator.getBuiltinDatapack(true, CompactMachines.MOD_ID, "basic_templates");
        basicTemplates.addProvider(output -> PackMetadataGenerator.forFeaturePack(
                output,
                Component.literal("Enables the basic room templates, built in to the mod.")
        ));

        if(event.includeServer()) {
            final var rtOut = basicTemplates.addProvider(output -> new RoomTemplatesGenerator(output, registryProvider));
            basicTemplates.addProvider(output -> new BasicRoomTemplateRecipeGenerator(output, rtOut.getRegistryProvider()));
        }

        if(event.includeClient()) {
            basicTemplates.addProvider(RoomTemplatesEnglishLangGenerator::new);
            basicTemplates.addProvider(RoomTemplatesFrenchLangGenerator::new);
        }
    }
}
