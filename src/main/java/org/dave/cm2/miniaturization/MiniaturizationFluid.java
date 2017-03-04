package org.dave.cm2.miniaturization;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.dave.cm2.CompactMachines2;

public class MiniaturizationFluid extends Fluid {
    public MiniaturizationFluid() {
        super(
                "miniaturization_fluid",
                new ResourceLocation(CompactMachines2.MODID + ":" + "blocks/miniaturization_fluid_still"),
                new ResourceLocation(CompactMachines2.MODID + ":" + "blocks/miniaturization_fluid_flowing")
        );
    }
}
