package org.dave.compactmachines3;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.dave.compactmachines3.command.CommandCompactMachines3;
import org.dave.compactmachines3.gui.GuiHandler;
import org.dave.compactmachines3.integration.CapabilityNullHandlerRegistry;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;
import org.dave.compactmachines3.misc.*;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.proxy.CommonProxy;
import org.dave.compactmachines3.render.BakeryHandler;
import org.dave.compactmachines3.schema.SchemaRegistry;
import org.dave.compactmachines3.skyworld.SkyChunkGenerator;
import org.dave.compactmachines3.skyworld.SkyDimension;
import org.dave.compactmachines3.skyworld.SkyWorldEvents;
import org.dave.compactmachines3.skyworld.SkyWorldSavedData;
import org.dave.compactmachines3.utility.AnnotatedInstanceUtil;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.world.ChunkLoadingMachines;
import org.dave.compactmachines3.world.ClientWorldData;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProviderRegistry;
import org.dave.compactmachines3.world.tools.DimensionTools;

@Mod(modid = CompactMachines3.MODID, version = CompactMachines3.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "after:refinedstorage;after:yunomakegoodmap", guiFactory = CompactMachines3.GUI_FACTORY)
public class CompactMachines3
{
    public static final String MODID = "compactmachines3";
    public static final String VERSION = "3.0.17";
    public static final String GUI_FACTORY = "org.dave.compactmachines3.misc.ConfigGuiFactory";

    @Mod.Instance(CompactMachines3.MODID)
    public static CompactMachines3 instance;

    public static ClientWorldData clientWorldData;

    public static final CreativeTabCompactMachines3 CREATIVE_TAB = new CreativeTabCompactMachines3();

    @SidedProxy(clientSide = "org.dave.compactmachines3.proxy.ClientProxy", serverSide = "org.dave.compactmachines3.proxy.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Logz.setLogger(event.getModLog());

        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());

        MinecraftForge.EVENT_BUS.register(MachineEventHandler.class);
        MinecraftForge.EVENT_BUS.register(PlayerEventHandler.class);
        MinecraftForge.EVENT_BUS.register(SkyWorldSavedData.class);
        MinecraftForge.EVENT_BUS.register(WorldSavedDataMachines.class);
        MinecraftForge.EVENT_BUS.register(RenderTickCounter.class);
        MinecraftForge.EVENT_BUS.register(BakeryHandler.class);
        MinecraftForge.EVENT_BUS.register(SkyWorldEvents.class);

        // Insist on keeping an already registered dimension by registering in pre-registerDimension.
        DimensionTools.registerDimension();

        GuiHandler.init();

        AnnotatedInstanceUtil.setAsmData(event.getAsmData());

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PackageHandler.init();

        proxy.init(event);

        SkyDimension.init();

        MultiblockRecipes.init();
        SchemaRegistry.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        CapabilityNullHandlerRegistry.registerNullHandlers();
        ExtraTileDataProviderRegistry.registerExtraTileDataProviders();

        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingMachines());

        proxy.postInit(event);
    }

    @EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        for(WorldServer world : event.getServer().worlds) {
            if(world.getChunkProvider().chunkGenerator instanceof SkyChunkGenerator) {
                world.setSpawnPoint(new BlockPos(0,0,0));
            }
        }
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCompactMachines3());
    }
}
