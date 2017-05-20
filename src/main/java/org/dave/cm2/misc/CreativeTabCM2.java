package org.dave.cm2.misc;

import mcjty.lib.compat.CompatCreativeTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.init.Itemss;

public class CreativeTabCM2 {
    public static final CreativeTabs CM2_TAB = new CompatCreativeTabs(CompactMachines2.MODID) {
        @Override
        protected Item getItem() {
            return Itemss.psd;
        }
    };
}
