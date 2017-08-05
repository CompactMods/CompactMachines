package org.dave.compactmachines3.block;

import net.minecraft.block.material.Material;
import org.dave.compactmachines3.misc.CreativeTabCompactMachines3;

public class BlockWallBreakable extends BlockBase {

    public BlockWallBreakable(Material material) {
        super(material);

        this.setHardness(3.0f);
        this.setResistance(128.0f);

        this.setCreativeTab(CreativeTabCompactMachines3.COMPACTMACHINES3_TAB);
    }
}
