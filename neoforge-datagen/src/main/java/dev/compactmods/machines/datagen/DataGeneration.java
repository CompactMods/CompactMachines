package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.datagen.base.BaseDatapack;
import dev.compactmods.machines.datagen.basic_room_templates.BasicRoomTemplatesDatapack;
import dev.compactmods.machines.feature.CMFeatureFlags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var baseResults = BaseDatapack.generatePack(event);
        BasicRoomTemplatesDatapack.generatePack(event, baseResults.holderLookupProvider());

        addExperimentalPacks(event);
    }

    private static void addExperimentalPacks(GatherDataEvent event) {
        final var generator = event.getGenerator();

        DataGenerator.PackGenerator roomUpgrades = generator.getBuiltinDatapack(true, CompactMachines.MOD_ID, "room_upgrades");
        roomUpgrades.addProvider(output -> PackMetadataGenerator.forFeaturePack(
                output,
                Component.literal("Enables the room upgrade experimental features."),
                FeatureFlagSet.of(CMFeatureFlags.ROOM_UPDATES_FLAG)
        ));
    }
}
