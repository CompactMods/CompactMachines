package org.dave.compactmachines3.item;

import net.minecraft.item.Item;
import org.dave.compactmachines3.CompactMachines3;

public class ItemBase extends Item {
    public ItemBase() {
        super();
    }

    @Override
    public Item setUnlocalizedName(String name) {
        if(!name.startsWith(CompactMachines3.MODID + ".")) {
            name = CompactMachines3.MODID + "." + name;
        }
        return super.setUnlocalizedName(name);
    }
}
