package org.dave.compactmachines3.init;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.compactmachines3.CompactMachines3;

public class Potionss {
    @GameRegistry.ObjectHolder("compactmachines3:miniaturizationpotion")
    public static Potion miniaturizationPotion;

    public static IAttribute scaleAttribute;

    public static void init() {
        scaleAttribute = new RangedAttribute(null, CompactMachines3.MODID + ".scale", 1.0f, 0.0125f, 1.0f).setShouldWatch(true);
    }
}
