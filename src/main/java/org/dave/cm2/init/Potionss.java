package org.dave.cm2.init;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.miniaturization.MiniaturizationPotion;

public class Potionss {
    public static Potion miniaturizationPotion;
    public static IAttribute scaleAttribute;

    public static void init() {
        miniaturizationPotion = new MiniaturizationPotion(false, 0x99A600).setRegistryName(CompactMachines2.MODID, "miniaturizationpotion");
        scaleAttribute = new RangedAttribute(null, CompactMachines2.MODID + ".scale", 1.0f, 0.0125f, 1.0f).setShouldWatch(true);

        registerPotions();
    }

    private static void registerPotions() {
        GameRegistry.register(miniaturizationPotion);
    }

}
