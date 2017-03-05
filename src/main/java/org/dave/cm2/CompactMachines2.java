package org.dave.cm2;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.init.Itemss;
import org.dave.cm2.init.Potionss;
import org.dave.cm2.miniaturization.MiniaturizationEvents;
import org.dave.cm2.miniaturization.MiniaturizationRecipes;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.network.PackageHandler;
import org.dave.cm2.proxy.CommonProxy;
import org.dave.cm2.world.ChunkLoadingMachines;
import org.dave.cm2.world.WorldSavedDataMachines;
import org.dave.cm2.world.tools.DimensionTools;

@Mod(modid = CompactMachines2.MODID, version = CompactMachines2.VERSION)
public class CompactMachines2
{
    public static final String MODID = "cm2";
    public static final String VERSION = "1.0";

    @Mod.Instance(CompactMachines2.MODID)
    public static CompactMachines2 instance;

    @SidedProxy(clientSide = "org.dave.cm2.proxy.ClientProxy", serverSide = "org.dave.cm2.proxy.ServerProxy")
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

        Fluidss.init();
        Blockss.init();
        Itemss.init();
        Potionss.init();
        MiniaturizationRecipes.init();

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PackageHandler.init();
        FMLInterModComms.sendMessage("Waila", "register", "org.dave.cm2.misc.WailaProvider.register");

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingMachines());
    }
}
