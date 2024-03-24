package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.api.util.KeyHelper;
import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOut, BlockTagGenerator blocks, CompletableFuture<HolderLookup.Provider> lookups) {
        super(packOut, lookups, blocks.contentsGetter());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        final var psd = Shrinking.PERSONAL_SHRINKING_DEVICE.get();

        machines();
        shrinkingDevices(psd);
        curiosTags(psd);
        upgrades();
    }

    private void upgrades() {
        var upgradeTag = tag(RoomUpgrade.ITEM_TAG);
        // upgradeTag.add(Items.IRON_AXE);
    }

    private void shrinkingDevices(PersonalShrinkingDevice psd) {
        final var cmShrinkTag = tag(PSDTags.ITEM);
        cmShrinkTag.add(psd);
        cmShrinkTag.addOptional(new ResourceLocation("shrink", "shrinking_device"));
    }

    private void curiosTags(PersonalShrinkingDevice psd) {
        final var curiosPsdTag = tag(TagKey.create(Registries.ITEMS.getRegistryKey(), new ResourceLocation("curios", "psd")));
        curiosPsdTag.add(psd);
    }

    private void machines() {
        var machinesTag = tag(MachineConstants.MACHINE_ITEM);

        var boundMachineItem = Machines.BOUND_MACHINE_BLOCK_ITEM.get();
        var unboundMachineItem = Machines.UNBOUND_MACHINE_BLOCK_ITEM.get();

        machinesTag.add(boundMachineItem);
        machinesTag.add(unboundMachineItem);
    }
}
