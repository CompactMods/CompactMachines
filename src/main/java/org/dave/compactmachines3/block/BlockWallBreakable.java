package org.dave.compactmachines3.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;

public class BlockWallBreakable extends BlockBase {

    public BlockWallBreakable(Material material) {
        super(material);

        this.setHardness(3.0f);
        this.setResistance(128.0f);

        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
