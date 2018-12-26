package org.dave.compactmachines3.proxy;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.block.*;
import org.dave.compactmachines3.compat.CompatHandler;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Triggerss;
import org.dave.compactmachines3.item.*;
import org.dave.compactmachines3.misc.SoundHandler;
import org.dave.compactmachines3.tile.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber
public class CommonProxy {
    @SubscribeEvent
    public static void onSoundRegistry(RegistryEvent.Register<SoundEvent> event) {
        SoundHandler.init(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockTunnel(Material.IRON).setTranslationKey("tunnel").setRegistryName(CompactMachines3.MODID, "tunnel"));
        GameRegistry.registerTileEntity(TileEntityTunnel.class, "TileEntityTunnel");

        event.getRegistry().register(new BlockWall(Material.IRON).setTranslationKey("wall").setRegistryName(CompactMachines3.MODID, "wall"));
        event.getRegistry().register(new BlockWallBreakable(Material.IRON).setTranslationKey("wallbreakable").setRegistryName(CompactMachines3.MODID, "wallbreakable"));

        event.getRegistry().register(new BlockMachine(Material.IRON).setTranslationKey("machine").setRegistryName(CompactMachines3.MODID, "machine"));
        GameRegistry.registerTileEntity(TileEntityMachine.class, "TileEntityMachine");

        event.getRegistry().register(new BlockFieldProjector(Material.IRON).setTranslationKey("fieldprojector").setRegistryName(CompactMachines3.MODID, "fieldprojector"));
        GameRegistry.registerTileEntity(TileEntityFieldProjector.class, "TileEntityFieldProjector");

        event.getRegistry().register(new BlockCraftingHologram(Material.IRON).setTranslationKey("craftinghologram").setRegistryName(CompactMachines3.MODID, "craftinghologram"));
        GameRegistry.registerTileEntity(TileEntityCraftingHologram.class, "TileEntityCraftingHologram");

        event.getRegistry().register(new BlockRedstoneTunnel(Material.IRON).setTranslationKey("redstonetunnel").setRegistryName(CompactMachines3.MODID, "redstonetunnel"));
        GameRegistry.registerTileEntity(TileEntityRedstoneTunnel.class, "TileEntityRedstoneTunnel");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(Blockss.tunnel).setRegistryName(Blockss.tunnel.getRegistryName()));
        event.getRegistry().register(new ItemBlock(Blockss.redstoneTunnel).setRegistryName(Blockss.redstoneTunnel.getRegistryName()));
        event.getRegistry().register(new ItemBlockWall(Blockss.wall).setRegistryName(Blockss.wall.getRegistryName()));
        event.getRegistry().register(new ItemBlock(Blockss.wallBreakable).setRegistryName(Blockss.wallBreakable.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(Blockss.machine).setRegistryName(Blockss.machine.getRegistryName()));
        event.getRegistry().register(new ItemBlockFieldProjector(Blockss.fieldProjector).setRegistryName(Blockss.fieldProjector.getRegistryName()));

        event.getRegistry().register(new ItemPersonalShrinkingDevice().setTranslationKey("psd").setRegistryName(CompactMachines3.MODID, "psd"));
        event.getRegistry().register(new ItemTunnelTool().setTranslationKey("tunneltool").setRegistryName(CompactMachines3.MODID, "tunneltool"));
        event.getRegistry().register(new ItemRedstoneTunnelTool().setTranslationKey("redstonetunneltool").setRegistryName(CompactMachines3.MODID, "redstonetunneltool"));
    }

    void registerTriggers() {
        Method method;
        try {
            method = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", "func_192118_a", ICriterionTrigger.class);
            method.setAccessible(true);
            method.invoke(null, Triggerss.IS_SKYWORLD);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void preInit(FMLPreInitializationEvent event) {
        CompatHandler.registerCompat();
    }

    public void init(FMLInitializationEvent event) {
        registerTriggers();
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void renderBlockMarker(double x, double y, double z) {
    }
}
