package org.dave.CompactMachines.init;

import org.dave.CompactMachines.item.ItemAtomEnlarger;
import org.dave.CompactMachines.item.ItemAtomShrinker;
import org.dave.CompactMachines.item.ItemEntangler;
import org.dave.CompactMachines.item.ItemInterface;
import org.dave.CompactMachines.item.ItemPersonalShrinkingDevice;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems
{
    public static final ItemPersonalShrinkingDevice personalShrinkingDevice = new ItemPersonalShrinkingDevice();
    public static final ItemAtomShrinker atomShrinker = new ItemAtomShrinker();
    public static final ItemAtomEnlarger atomEnlarger = new ItemAtomEnlarger();
    public static final ItemInterface interfaceItem = new ItemInterface();
    public static final ItemEntangler quantumEntangler = new ItemEntangler();

    public static void init()
    {
        GameRegistry.registerItem(personalShrinkingDevice, Names.Items.PSD);
        GameRegistry.registerItem(atomShrinker, Names.Items.SHRINKER);
        GameRegistry.registerItem(atomEnlarger, Names.Items.ENLARGER);
        GameRegistry.registerItem(interfaceItem, Names.Items.INTERFACEITEM);
        GameRegistry.registerItem(quantumEntangler, Names.Items.QUANTUMENTANGLER);
    }
}
