package org.dave.cm2.item;

import net.minecraft.item.Item;
import org.dave.cm2.CompactMachines2;

public class ItemBase extends Item {
    public ItemBase() {
        super();
    }

    @Override
    public Item setUnlocalizedName(String name) {
        if(!name.startsWith(CompactMachines2.MODID + ".")) {
            name = CompactMachines2.MODID + "." + name;
        }
        return super.setUnlocalizedName(name);
    }
}
