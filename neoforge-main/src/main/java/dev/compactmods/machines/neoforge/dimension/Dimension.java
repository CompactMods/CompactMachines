package dev.compactmods.machines.neoforge.dimension;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.neoforge.Registries;
import net.neoforged.neoforge.registries.DeferredBlock;

public class Dimension {

    public static final DeferredBlock<VoidAirBlock> BLOCK_MACHINE_VOID_AIR = Registries.BLOCKS.register("machine_void_air", VoidAirBlock::new);

    public static void prepare() {

    }
}
