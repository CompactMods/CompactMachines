package org.dave.compactmachines3.miniaturization;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.dave.compactmachines3.CompactMachines3;

public class MiniaturizationFluid extends Fluid {
    public MiniaturizationFluid() {
        super(
                "miniaturization_fluid",
                new ResourceLocation(CompactMachines3.MODID + ":" + "blocks/miniaturization_fluid_still"),
                new ResourceLocation(CompactMachines3.MODID + ":" + "blocks/miniaturization_fluid_flowing")
        );
    }
}
