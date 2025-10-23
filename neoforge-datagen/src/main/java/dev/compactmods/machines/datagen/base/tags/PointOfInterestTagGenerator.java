package dev.compactmods.machines.datagen.base.tags;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PointOfInterestTagGenerator extends PoiTypeTagsProvider {
    public PointOfInterestTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFiles) {
        super(packOut, lookup, CompactMachines.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        //        builder.add(Villagers.TINKERER_WORKBENCH_KEY);
    }
}
