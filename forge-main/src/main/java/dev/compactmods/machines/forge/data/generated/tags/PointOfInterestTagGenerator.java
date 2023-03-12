package dev.compactmods.machines.forge.data.generated.tags;

import dev.compactmods.machines.forge.villager.Villagers;
import dev.compactmods.machines.api.core.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class PointOfInterestTagGenerator extends PoiTypeTagsProvider {
    public PointOfInterestTagGenerator(DataGenerator gen, @Nullable ExistingFileHelper files) {
        super(gen, Constants.MOD_ID, files);
    }

    @Override
    protected void addTags() {
        TagAppender<PoiType> builder = tag(PoiTypeTags.ACQUIRABLE_JOB_SITE);
        builder.add(Villagers.TINKERER_WORKBENCH_KEY);
    }
}
