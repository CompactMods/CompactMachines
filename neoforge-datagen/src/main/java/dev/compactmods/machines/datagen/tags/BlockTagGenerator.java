package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.room.Rooms;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput packOut, ExistingFileHelper files, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOut, lookup, Constants.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var allMachines = tag(MachineConstants.MACHINE_BLOCK);
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var ironTool = tag(BlockTags.NEEDS_IRON_TOOL);

        var breakableWall = Rooms.BLOCK_BREAKABLE_WALL.get();
        pickaxe.add(breakableWall);
        ironTool.add(breakableWall);

        var boundMachine = Machines.MACHINE_BLOCK.get();
        allMachines.add(boundMachine);
        pickaxe.add(boundMachine);
        ironTool.add(boundMachine);

        var unboundTag = tag(MachineConstants.UNBOUND_MACHINE_BLOCK);
        var unboundMachine = Machines.UNBOUND_MACHINE_BLOCK.get();
        allMachines.add(unboundMachine);
        unboundTag.add(unboundMachine);
        pickaxe.add(unboundMachine);
        ironTool.add(unboundMachine);
    }
}
