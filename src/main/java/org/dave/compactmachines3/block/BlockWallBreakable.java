package org.dave.compactmachines3.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockWallBreakable extends Block {

    public BlockWallBreakable() {
        super(Block.Properties
                .create(Material.IRON)
                .hardnessAndResistance(3.0f, 128.0f));

        // this.setCreativeTab(CompactMachines3.CREATIVE_TAB);
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }
}
