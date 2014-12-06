package org.dave.CompactMachines.creativetab;

import org.dave.CompactMachines.init.ModItems;
import org.dave.CompactMachines.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabCM
{
	public static final CreativeTabs	CM_TAB	= new CreativeTabs(Reference.MOD_ID.toLowerCase())
												{
													@Override
													public Item getTabIconItem()
													{
														return ModItems.psd;
													}
												};
}
