package dev.compactmods.machines.data.generated.tags;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.Registries;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(DataGenerator gen, BlockTagsProvider blockTags, @Nullable ExistingFileHelper files) {
        super(gen, blockTags, Constants.MOD_ID, files);
    }

    @Override
    protected void addTags() {
        var upgradeTag = tag(CMTags.ROOM_UPGRADE_ITEM);
        var machinesTag = tag(CMTags.MACHINE_ITEM);

        var legacySizedMachines = Set.of(Machines.MACHINE_BLOCK_ITEM_TINY.get(),
                Machines.MACHINE_BLOCK_ITEM_SMALL.get(),
                Machines.MACHINE_BLOCK_ITEM_NORMAL.get(),
                Machines.MACHINE_BLOCK_ITEM_LARGE.get(),
                Machines.MACHINE_BLOCK_ITEM_GIANT.get(),
                Machines.MACHINE_BLOCK_ITEM_MAXIMUM.get());

        var boundMachineItem = Machines.BOUND_MACHINE_BLOCK_ITEM.get();
        var unboundMachineItem = Machines.UNBOUND_MACHINE_BLOCK_ITEM.get();

        legacySizedMachines.forEach(machinesTag::add);
        machinesTag.add(boundMachineItem);
        machinesTag.add(unboundMachineItem);

        upgradeTag.add(MachineRoomUpgrades.ROOM_UPGRADE.get());

        final var psd = Shrinking.PERSONAL_SHRINKING_DEVICE.get();
        final var curiosPsdTag = tag(TagKey.create(Registries.ITEMS.getRegistryKey(), new ResourceLocation("curios", "psd")));
        final var cmShrinkTag = tag(PSDTags.ITEM);
        curiosPsdTag.add(psd);
        cmShrinkTag.add(psd);
    }
}
