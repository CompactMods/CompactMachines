package org.dave.compactmachines3.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import org.dave.compactmachines3.CompactMachines3;

public class BlockBase extends Block {
    public BlockBase(Material material) {
        super(material);
    }

    public BlockBase() {
        this(Material.ROCK);
    }

    @Override
    public Block setUnlocalizedName(String name) {
        if(!name.startsWith(CompactMachines3.MODID + ".")) {
            name = CompactMachines3.MODID + "." + name;
        }
        return super.setUnlocalizedName(name);
    }
}
