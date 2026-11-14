package dev.compactmods.machines.test.gametest.core;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = CompactMachinesCore.MOD_ID)
public class CompactMachinesTest {

    public CompactMachinesTest(ModContainer container, IEventBus modBus) {
        // Exit if we aren't in a dedicated gametest setup
        if (System.getenv().containsKey("CM_GAMETEST_ENABLED") && !System.getenv("CM_GAMETEST_ENABLED").equals("true"))
            return;

        CMTestFramework.init(container, modBus);
    }
}
