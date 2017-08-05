package org.dave.compactmachines3.init;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.dave.compactmachines3.miniaturization.MiniaturizationFluid;

public class Fluidss {
    public static Fluid miniaturizationFluid;

    public static void init() {
        miniaturizationFluid = new MiniaturizationFluid();

        registerFluids();
    }

    private static void registerFluids() {
        FluidRegistry.registerFluid(miniaturizationFluid);
        FluidRegistry.addBucketForFluid(miniaturizationFluid);
    }
}
