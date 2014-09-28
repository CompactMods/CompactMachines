package org.dave.CompactMachines;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;

import org.dave.CompactMachines.handler.CMEventHandler;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.GuiHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler.SharedStorageSaveHandler;
import org.dave.CompactMachines.handler.machinedimension.MachineHandler;
import org.dave.CompactMachines.handler.machinedimension.MachineWorldChunkloadCallback;
import org.dave.CompactMachines.handler.machinedimension.WorldProviderMachines;
import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.init.ModItems;
import org.dave.CompactMachines.init.Recipes;
import org.dave.CompactMachines.network.PacketHandler;
import org.dave.CompactMachines.proxy.IProxy;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.utility.LogHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.network.NetworkRegistry;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "after:appliedenergistics2;after:ProjRed|Transmission")
public class CompactMachines {
	@Mod.Instance(Reference.MOD_ID)
    public static CompactMachines instance;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;

	public MachineHandler machineHandler;

    @Mod.EventHandler
    public void preServerStart(FMLServerAboutToStartEvent event) {
        SharedStorageHandler.reloadStorageHandler(false);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(new ConfigurationHandler());

        Reference.AE_AVAILABLE = Loader.isModLoaded("appliedenergistics2");
        Reference.PR_AVAILABLE = Loader.isModLoaded("ProjRed|Transmission");
        Reference.OC_AVAILABLE = Loader.isModLoaded("OpenComputers");

        // Insist on keeping an already registered dimension by registering in pre-init.
        if(ConfigurationHandler.dimensionId != -1) {
            DimensionManager.registerProviderType(ConfigurationHandler.dimensionId, WorldProviderMachines.class, true);
            DimensionManager.registerDimension(ConfigurationHandler.dimensionId, ConfigurationHandler.dimensionId);
        }

        ModItems.init();
        ModBlocks.init();

		CMEventHandler rte = new CMEventHandler();
		MinecraftForge.EVENT_BUS.register(rte);
		MinecraftForge.EVENT_BUS.register(new SharedStorageSaveHandler());

		FMLCommonHandler.instance().bus().register(rte);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
    	PacketHandler.init();
    	NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

    	proxy.registerTileEntities();

        Recipes.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if(ConfigurationHandler.dimensionId == -1) {
        	int dimensionId = DimensionManager.getNextFreeDimId();
        	LogHelper.info("Using dimension " + dimensionId + " as machine dimension.");
        	ConfigurationHandler.dimensionId = dimensionId;
        	ConfigurationHandler.saveConfiguration();

            DimensionManager.registerProviderType(ConfigurationHandler.dimensionId, WorldProviderMachines.class, true);
            DimensionManager.registerDimension(ConfigurationHandler.dimensionId, ConfigurationHandler.dimensionId);
        }

    	ForgeChunkManager.setForcedChunkLoadingCallback(instance, new MachineWorldChunkloadCallback());
    }
}
