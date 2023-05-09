package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.forge.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Set;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(DataGenerator generator, ExistingFileHelper files) {
        super(generator, Constants.MOD_ID, files);
    }

    @Override
    @SuppressWarnings("removal")
    public void addTags() {
        var legacySizedMachines = Set.of(Machines.MACHINE_BLOCK_TINY.get(),
                Machines.MACHINE_BLOCK_SMALL.get(),
                Machines.MACHINE_BLOCK_NORMAL.get(),
                Machines.MACHINE_BLOCK_LARGE.get(),
                Machines.MACHINE_BLOCK_GIANT.get(),
                Machines.MACHINE_BLOCK_MAXIMUM.get());

        var legacyMachines = tag(LegacySizedCompactMachineBlock.LEGACY_MACHINES_TAG);
        var allMachines = tag(CMTags.MACHINE_BLOCK);
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        var breakableWall = Walls.BLOCK_BREAKABLE_WALL.get();
        pickaxe.add(breakableWall);
        ironTool.add(breakableWall);

        legacySizedMachines.forEach(mach -> {
            legacyMachines.add(mach);
            allMachines.add(mach);
            pickaxe.add(mach);
            ironTool.add(mach);
        });

        var boundMachine = Machines.MACHINE_BLOCK.get();
        allMachines.add(boundMachine);
        pickaxe.add(boundMachine);
        ironTool.add(boundMachine);

        var unboundTag = tag(CMTags.UNBOUND_MACHINE_BLOCK);
        var unboundMachine = Machines.UNBOUND_MACHINE_BLOCK.get();
        allMachines.add(unboundMachine);
        unboundTag.add(unboundMachine);
        pickaxe.add(unboundMachine);
        ironTool.add(unboundMachine);
    }
}
