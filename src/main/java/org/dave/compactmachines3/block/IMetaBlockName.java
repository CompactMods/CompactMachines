package org.dave.compactmachines3.block;

import net.minecraft.item.ItemStack;

/*
 This interface and concept is taken from:
   http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/blockstates-and-metadata/
 Thanks! And look there for details!
 */
public interface IMetaBlockName {
    String getSpecialName(ItemStack stack);
}
