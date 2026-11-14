package dev.compactmods.machines.dimension;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Dimension {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachinesCore.MOD_ID);

    public static final DeferredBlock<VoidAirBlock> BLOCK_MACHINE_VOID_AIR = BLOCKS.register("machine_void_air", VoidAirBlock::new);

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
