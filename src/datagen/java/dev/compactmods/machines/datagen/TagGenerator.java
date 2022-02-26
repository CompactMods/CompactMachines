package dev.compactmods.machines.datagen;

import java.util.Set;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TagGenerator extends BlockTagsProvider {

    public TagGenerator(DataGenerator generator, ExistingFileHelper files) {
        super(generator, CompactMachines.MOD_ID, files);
    }

    @Override
    public void addTags() {
        var machines = Set.of(Registration.MACHINE_BLOCK_TINY.get(),
                Registration.MACHINE_BLOCK_SMALL.get(),
                Registration.MACHINE_BLOCK_NORMAL.get(),
                Registration.MACHINE_BLOCK_LARGE.get(),
                Registration.MACHINE_BLOCK_GIANT.get(),
                Registration.MACHINE_BLOCK_MAXIMUM.get());

        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        var breakableWall = Registration.BLOCK_BREAKABLE_WALL.get();
        pickaxe.add(breakableWall);
        ironTool.add(breakableWall);

        machines.forEach(mach -> {
            pickaxe.add(mach);
            ironTool.add(mach);
        });

    }
}
