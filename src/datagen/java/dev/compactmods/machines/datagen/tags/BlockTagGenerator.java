package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput packOut, ExistingFileHelper files, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOut, lookup, Constants.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
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
