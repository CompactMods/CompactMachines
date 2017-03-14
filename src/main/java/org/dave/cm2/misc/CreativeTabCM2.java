package org.dave.cm2.misc;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.init.Itemss;

public class CreativeTabCM2 {
    public static final CreativeTabs CM2_TAB = new CreativeTabs(CompactMachines2.MODID) {
        @Override
        public Item getTabIconItem() {
            return Itemss.psd;
        }
    };
}
