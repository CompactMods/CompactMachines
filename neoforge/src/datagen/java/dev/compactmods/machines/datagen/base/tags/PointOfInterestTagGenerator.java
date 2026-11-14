package dev.compactmods.machines.datagen.base.tags;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PointOfInterestTagGenerator extends PoiTypeTagsProvider {
    public PointOfInterestTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOut, lookup, CompactMachinesCore.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        var builder = tag(PoiTypeTags.ACQUIRABLE_JOB_SITE);
        builder.add(Villagers.TINKERER_WORKBENCH_KEY);
    }
}
