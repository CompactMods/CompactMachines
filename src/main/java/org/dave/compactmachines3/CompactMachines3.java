package org.dave.compactmachines3;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.compactmachines3.command.CommandCompactMachines3;
import org.dave.compactmachines3.gui.GuiHandler;
import org.dave.compactmachines3.init.*;
import org.dave.compactmachines3.integration.CapabilityNullHandlerRegistry;
import org.dave.compactmachines3.miniaturization.MiniaturizationEvents;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.misc.Villager;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.proxy.CommonProxy;
import org.dave.compactmachines3.world.ChunkLoadingMachines;
import org.dave.compactmachines3.world.WorldGenMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;

@Mod(modid = CompactMachines3.MODID, version = CompactMachines3.VERSION)
public class CompactMachines3
{
    public static final String MODID = "compactmachines3";
    public static final String VERSION = "3.0.0";

    @Mod.Instance(CompactMachines3.MODID)
    public static CompactMachines3 instance;

    @SidedProxy(clientSide = "org.dave.compactmachines3.proxy.ClientProxy", serverSide = "org.dave.compactmachines3.proxy.ServerProxy")
    public static CommonProxy proxy;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());

        MinecraftForge.EVENT_BUS.register(WorldSavedDataMachines.class);
        MinecraftForge.EVENT_BUS.register(MiniaturizationEvents.class);

        // Insist on keeping an already registered dimension by registering in pre-registerDimension.
        DimensionTools.registerDimension();

        GameRegistry.registerWorldGenerator(new WorldGenMachines(), -1024);

        GuiHandler.init();
        Fluidss.init();

        CapabilityNullHandlerRegistry.registerNullHandlers(event.getAsmData());

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PackageHandler.init();
        FMLInterModComms.sendMessage("Waila", "register", "org.dave.compactmachines3.misc.WailaProvider.register");

        proxy.init(event);

        MultiblockRecipes.init();
        Recipes.init();
        Villager.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingMachines());
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCompactMachines3());
    }
}
