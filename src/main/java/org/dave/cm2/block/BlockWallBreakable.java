package org.dave.cm2.block;

import net.minecraft.block.material.Material;
import org.dave.cm2.misc.CreativeTabCM2;

public class BlockWallBreakable extends BlockBase {

    public BlockWallBreakable(Material material) {
        super(material);

        this.setHardness(3.0f);
        this.setResistance(128.0f);

        this.setCreativeTab(CreativeTabCM2.CM2_TAB);
    }
}
