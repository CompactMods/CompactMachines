package dev.compactmods.machines.datagen.base.tags;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.WallConstants;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {

    public BlockTagGenerator(PackOutput packOut, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOut, lookup, CompactMachinesCore.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        final var breakableWall = Rooms.Blocks.BREAKABLE_WALL.getKey();
        final var solidWall = Rooms.Blocks.SOLID_WALL.getKey();
        final var machineBlock = Machines.Blocks.MACHINE.getKey();
        final var voidAir = Dimension.BLOCK_MACHINE_VOID_AIR.getKey();
        final var spatialWorkbench = Villagers.SPATIAL_WORKBENCH.getKey();
        final var tunnelWall = Tunnels.Blocks.TUNNEL_WALL.getKey();

        tag(WallConstants.TAG_SOLID_WALL_BLOCKS)
                .add(solidWall)
                .add(tunnelWall);

        tag(MachineConstants.MACHINE_BLOCK)
                .add(machineBlock);

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(breakableWall)
                .add(machineBlock);

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(breakableWall)
                .add(machineBlock);

        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .add(machineBlock)
                .add(solidWall)
                .add(tunnelWall)
                .add(voidAir);

        tag(Tags.Blocks.VILLAGER_JOB_SITES)
                .add(spatialWorkbench);
    }
}
