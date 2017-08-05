package org.dave.compactmachines3.init;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.miniaturization.MiniaturizationPotion;

public class Potionss {
    public static Potion miniaturizationPotion;
    public static IAttribute scaleAttribute;

    public static void init() {
        miniaturizationPotion = new MiniaturizationPotion(false, 0x99A600).setRegistryName(CompactMachines3.MODID, "miniaturizationpotion");
        scaleAttribute = new RangedAttribute(null, CompactMachines3.MODID + ".scale", 1.0f, 0.0125f, 1.0f).setShouldWatch(true);

        registerPotions();
    }

    private static void registerPotions() {
        GameRegistry.register(miniaturizationPotion);
    }

}
