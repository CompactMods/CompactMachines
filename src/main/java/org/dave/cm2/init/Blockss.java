package org.dave.cm2.init;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.block.*;
import org.dave.cm2.item.ItemBlockMachine;
import org.dave.cm2.item.ItemBlockWall;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.tile.TileEntityTunnel;

public class Blockss {
    public static Block tunnel;
    public static Block wall;
    public static Block wallBreakable;
    public static Block machine;
    public static Block miniaturizationFluidBlock;

    public static void init() {
        tunnel = new BlockTunnel(Material.IRON).setUnlocalizedName("tunnel").setRegistryName(CompactMachines2.MODID, "tunnel");
        wall = new BlockWall(Material.IRON).setUnlocalizedName("wall").setRegistryName(CompactMachines2.MODID, "wall");
        wallBreakable = new BlockWallBreakable(Material.IRON).setUnlocalizedName("wallbreakable").setRegistryName(CompactMachines2.MODID, "wallbreakable");

        machine = new BlockMachine(Material.IRON).setUnlocalizedName("machine").setRegistryName(CompactMachines2.MODID, "machine");
        miniaturizationFluidBlock = new BlockMiniaturizationFluid().setRegistryName(CompactMachines2.MODID, "miniaturization_fluid_block");

        registerBlocks();
    }

    private static void registerBlocks() {
        GameRegistry.register(tunnel);
        GameRegistry.register(new ItemBlock(tunnel).setRegistryName(tunnel.getRegistryName()));
        GameRegistry.registerTileEntity(TileEntityTunnel.class, "TileEntityTunnel");

        GameRegistry.register(wall);
        GameRegistry.register(new ItemBlockWall(wall).setRegistryName(wall.getRegistryName()));

        GameRegistry.register(wallBreakable);
        GameRegistry.register(new ItemBlock(wallBreakable).setRegistryName(wallBreakable.getRegistryName()));


        GameRegistry.register(machine);
        GameRegistry.register(new ItemBlockMachine(machine).setRegistryName(machine.getRegistryName()));
        GameRegistry.registerTileEntity(TileEntityMachine.class, "TileEntityMachine");

        GameRegistry.register(miniaturizationFluidBlock);
        GameRegistry.register(new ItemBlock(miniaturizationFluidBlock).setRegistryName(miniaturizationFluidBlock.getRegistryName()));
    }
}
