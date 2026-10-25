package dev.compactmods.machines.feature;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class CMFeaturePacks {

    public static void addFeaturePacks(final AddPackFindersEvent event) {
        addOptionalRoomTemplateDataPack(event, CompactMachinesCore.identifier("basic_templates"), Component.literal("Compact Machines: Basic Room Templates"));
        addOptionalFeaturePack(event, CompactMachinesCore.identifier("room_upgrades"), Component.literal("Compact Machines: Room Upgrades"));

        // Debugging nodes for the room upgrade canvas: dev runs only, and always on there
        if (!FMLEnvironment.isProduction()) {
            event.addPackFinders(
                    CompactMachinesCore.identifier("data/" + CompactMachinesCore.MOD_ID + "/datapacks/debug_nodes"),
                    PackType.SERVER_DATA,
                    Component.literal("Compact Machines: Debug Nodes"),
                    PackSource.BUILT_IN,
                    true,
                    Pack.Position.TOP
            );
        }
    }

    private static void addOptionalFeaturePack(AddPackFindersEvent event, Identifier packName, Component displayName) {
        event.addPackFinders(
                CompactMachinesCore.identifier("data/" + packName.getNamespace() + "/datapacks/" + packName.getPath()),
                PackType.SERVER_DATA,
                displayName,
                PackSource.FEATURE,
                false,
                Pack.Position.TOP
        );
    }

    private static void addOptionalRoomTemplateDataPack(AddPackFindersEvent event, Identifier packName, Component displayName) {
        event.addPackFinders(
                CompactMachinesCore.identifier("data/" + packName.getNamespace() + "/datapacks/" + packName.getPath()),
                PackType.SERVER_DATA,
                displayName,
                PackSource.FEATURE,
                false,
                Pack.Position.TOP
        );
    }
}
