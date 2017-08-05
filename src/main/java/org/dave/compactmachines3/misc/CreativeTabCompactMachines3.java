package org.dave.compactmachines3.misc;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.init.Itemss;

public class CreativeTabCompactMachines3 {
    public static final CreativeTabs COMPACTMACHINES3_TAB = new CreativeTabs(CompactMachines3.MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(Itemss.psd);
        }
    };
}
