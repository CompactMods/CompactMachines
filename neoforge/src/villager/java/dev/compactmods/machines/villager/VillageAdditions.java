package dev.compactmods.machines.villager;

import com.mojang.datafixers.util.Pair;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

public class VillageAdditions {
    static void injectVillageBuildings(final ServerAboutToStartEvent evt) {
        var templatePools = evt.getServer().registryAccess()
                .lookupOrThrow(Registries.TEMPLATE_POOL);

        var processorLists = evt.getServer().registryAccess()
                .lookupOrThrow(Registries.PROCESSOR_LIST);

        addBuildingToPool(templatePools, processorLists,
                Identifier.withDefaultNamespace("village/plains/houses"),
                CompactMachinesCore.identifier("village/plains/tinkerer_area"), 5);
    }

    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, Identifier.withDefaultNamespace("empty"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          Registry<StructureProcessorList> processorListRegistry,
                                          Identifier poolRL,
                                          Identifier nbtPieceRL,
                                          int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.getValue(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        // Use .legacy( for villages/outposts and .single( for everything else
        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL.toString(), emptyProcessorList)
                .apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        var listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }
}
