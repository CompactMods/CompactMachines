package org.dave.compactmachines3.item;

import net.minecraft.item.Item;
import org.dave.compactmachines3.CompactMachines3;

public class ItemBase extends Item {
    public ItemBase() {
        super();
    }

    @Override
    public Item setTranslationKey(String name) {
        if(!name.startsWith(CompactMachines3.MODID + ".")) {
            name = CompactMachines3.MODID + "." + name;
        }
        return super.setTranslationKey(name);
    }
}
