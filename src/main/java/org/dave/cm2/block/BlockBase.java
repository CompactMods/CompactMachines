package org.dave.cm2.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import org.dave.cm2.CompactMachines2;

public class BlockBase extends Block {
    public BlockBase(Material material) {
        super(material);
    }

    public BlockBase() {
        this(Material.ROCK);
    }

    @Override
    public Block setUnlocalizedName(String name) {
        if(!name.startsWith(CompactMachines2.MODID + ".")) {
            name = CompactMachines2.MODID + "." + name;
        }
        return super.setUnlocalizedName(name);
    }
}
