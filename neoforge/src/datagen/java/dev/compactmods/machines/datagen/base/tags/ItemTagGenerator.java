package dev.compactmods.machines.datagen.base.tags;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookups) {
        super(packOut, lookups, CompactMachinesCore.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        final var psd = Shrinking.Items.PERSONAL_SHRINKING_DEVICE.getKey();

        machines();
        curiosTags(psd);
    }

    private void curiosTags(ResourceKey<Item> psd) {
        final var curiosPsdTag = tag(TagKey.create(psd.registryKey(), Identifier.fromNamespaceAndPath("curios", "psd")));
        curiosPsdTag.add(psd);
    }

    private void machines() {
        var machinesTag = tag(MachineConstants.MACHINE_ITEM);
        var boundMachineItem = Machines.Items.MACHINE.getKey();

        machinesTag.add(boundMachineItem);
    }
}
