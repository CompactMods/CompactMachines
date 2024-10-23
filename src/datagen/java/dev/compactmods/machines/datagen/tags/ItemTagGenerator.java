package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOut, BlockTagGenerator blocks, CompletableFuture<HolderLookup.Provider> lookups) {
        super(packOut, lookups, blocks.contentsGetter());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var upgradeTag = tag(CMTags.ROOM_UPGRADE_ITEM);

        upgradeTag.add(MachineRoomUpgrades.CHUNKLOADER.get());
    }
}
