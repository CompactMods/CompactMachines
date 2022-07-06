package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(DataGenerator gen, BlockTagsProvider blockTags, @Nullable ExistingFileHelper files) {
        super(gen, blockTags, CompactMachines.MOD_ID, files);
    }

    @Override
    protected void addTags() {
        var upgradeTag = tag(CMTags.ROOM_UPGRADE_ITEM);

        upgradeTag.add(MachineRoomUpgrades.CHUNKLOADER.get());
    }
}
