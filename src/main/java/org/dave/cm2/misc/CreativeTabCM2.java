package org.dave.cm2.misc;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.init.Itemss;

public class CreativeTabCM2 {
    public static final CreativeTabs CM2_TAB = new CreativeTabs(CompactMachines2.MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(Itemss.psd);
        }
    };
}
