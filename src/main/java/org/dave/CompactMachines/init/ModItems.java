package org.dave.CompactMachines.init;

import org.dave.CompactMachines.item.ItemAtomEnlarger;
import org.dave.CompactMachines.item.ItemAtomShrinker;
import org.dave.CompactMachines.item.ItemCM;
import org.dave.CompactMachines.item.ItemInterface;
import org.dave.CompactMachines.item.ItemPersonalShrinkingDevice;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems
{
    public static final ItemCM personalShrinkingDevice = new ItemPersonalShrinkingDevice();
    public static final ItemCM atomShrinker = new ItemAtomShrinker();
    public static final ItemCM atomEnlarger = new ItemAtomEnlarger();
    public static final ItemCM interfaceItem = new ItemInterface();

    public static void init()
    {
        GameRegistry.registerItem(personalShrinkingDevice, Names.Items.PSD);
        GameRegistry.registerItem(atomShrinker, Names.Items.SHRINKER);
        GameRegistry.registerItem(atomEnlarger, Names.Items.ENLARGER);
        GameRegistry.registerItem(interfaceItem, Names.Items.INTERFACEITEM);
    }
}
