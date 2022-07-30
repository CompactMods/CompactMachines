package dev.compactmods.machines.datagen.tags;

import java.util.Set;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(DataGenerator generator, ExistingFileHelper files) {
        super(generator, CompactMachines.MOD_ID, files);
    }

    @Override
    public void addTags() {
        var machines = Set.of(Machines.MACHINE_BLOCK_TINY.get(),
                Machines.MACHINE_BLOCK_SMALL.get(),
                Machines.MACHINE_BLOCK_NORMAL.get(),
                Machines.MACHINE_BLOCK_LARGE.get(),
                Machines.MACHINE_BLOCK_GIANT.get(),
                Machines.MACHINE_BLOCK_MAXIMUM.get());

        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        var breakableWall = Walls.BLOCK_BREAKABLE_WALL.get();
        pickaxe.add(breakableWall);
        ironTool.add(breakableWall);

        machines.forEach(mach -> {
            pickaxe.add(mach);
            ironTool.add(mach);
        });
    }
}
