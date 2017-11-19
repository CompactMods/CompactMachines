package org.dave.compactmachines3.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.block.*;

public class Blockss {
    @GameRegistry.ObjectHolder("compactmachines3:tunnel")
    public static BlockTunnel tunnel;

    @GameRegistry.ObjectHolder("compactmachines3:wall")
    public static BlockWall wall;

    @GameRegistry.ObjectHolder("compactmachines3:wallbreakable")
    public static BlockWallBreakable wallBreakable;

    @GameRegistry.ObjectHolder("compactmachines3:machine")
    public static BlockMachine machine;

    @GameRegistry.ObjectHolder("compactmachines3:fieldprojector")
    public static BlockFieldProjector fieldProjector;

    @GameRegistry.ObjectHolder("compactmachines3:craftinghologram")
    public static BlockCraftingHologram craftingHologram;

    @GameRegistry.ObjectHolder("compactmachines3:redstonetunnel")
    public static BlockRedstoneTunnel redstoneTunnel;

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        tunnel.initModel();
        wall.initModel();
        wallBreakable.initModel();
        machine.initModel();
        fieldProjector.initModel();
        craftingHologram.initModel();
        redstoneTunnel.initModel();
    }

}
