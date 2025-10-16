package dev.compactmods.machines.datagen.base.tags;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookups) {
        super(packOut, lookups, CompactMachines.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        final var psd = Shrinking.PERSONAL_SHRINKING_DEVICE.get();

        machines();
        curiosTags(psd);
    }

    private void curiosTags(PersonalShrinkingDevice psd) {
        final var curiosPsdTag = tag(TagKey.create(CMRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath("curios", "psd")));
        curiosPsdTag.add(psd);
    }

    private void machines() {
        var machinesTag = tag(MachineConstants.MACHINE_ITEM);
        var boundMachines = tag(MachineConstants.BOUND_MACHINE_ITEM);
        var unboundMachines = tag(MachineConstants.NEW_MACHINE_ITEM);

        var boundMachineItem = Machines.Items.BOUND_MACHINE.get();
        var unboundMachineItem = Machines.Items.UNBOUND_MACHINE.get();

        machinesTag.add(boundMachineItem);
        machinesTag.add(unboundMachineItem);
        boundMachines.add(boundMachineItem);
        unboundMachines.add(unboundMachineItem);
    }
}
