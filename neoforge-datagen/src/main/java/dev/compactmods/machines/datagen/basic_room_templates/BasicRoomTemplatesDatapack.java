package dev.compactmods.machines.datagen.basic_room_templates;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.datagen.basic_room_templates.lang.RoomTemplatesEnglishLangGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class BasicRoomTemplatesDatapack {

    public static void generatePack(GatherDataEvent event) {
        final var generator = event.getGenerator();
        final var lookupProvider = event.getLookupProvider();

        DataGenerator.PackGenerator basicTemplates = generator.getBuiltinDatapack(true, CompactMachines.MOD_ID, "basic_templates");
        basicTemplates.addProvider(output -> PackMetadataGenerator.forFeaturePack(
                output,
                Component.literal("Enables the basic room templates, built in to the mod.")
        ));

        basicTemplates.addProvider(output -> new RoomTemplatesGenerator(output, lookupProvider));
        event.createProvider((output,provider)
                -> new BasicRoomTemplateRecipeGenerator.Runner(CompactMachines.dotPrefix("basic_room_templates"), output, provider));

        basicTemplates.addProvider(RoomTemplatesEnglishLangGenerator::new);
    }
}
