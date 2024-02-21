package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOut, BlockTagGenerator blocks, CompletableFuture<HolderLookup.Provider> lookups) {
        super(packOut, lookups, blocks.contentsGetter());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // var upgradeTag = tag(CMTags.ROOM_UPGRADE_ITEM);
        var machinesTag = tag(MachineConstants.MACHINE_ITEM);

        var boundMachineItem = Machines.BOUND_MACHINE_BLOCK_ITEM.get();
        var unboundMachineItem = Machines.UNBOUND_MACHINE_BLOCK_ITEM.get();

        machinesTag.add(boundMachineItem);
        machinesTag.add(unboundMachineItem);

        final var psd = Shrinking.PERSONAL_SHRINKING_DEVICE.get();
        final var curiosPsdTag = tag(TagKey.create(Registries.ITEMS.getRegistryKey(), new ResourceLocation("curios", "psd")));
        final var cmShrinkTag = tag(PSDTags.ITEM);
        curiosPsdTag.add(psd);
        cmShrinkTag.add(psd);
    }
}
