package com.robotgryphon.compactmachines;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod(CompactMachines.MODID)
public class CompactMachines
{
    public static final String MODID = "compactmachines";

    public static final Logger LOGGER = LogManager.getLogger();

//    public static ClientWorldData clientWorldData;
//    public static final CreativeTabcompactmachines CREATIVE_TAB = new CreativeTabcompactmachines();

    public static ItemGroup COMPACT_MACHINES_ITEMS = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registrations.MACHINE_BLOCK_ITEM_NORMAL.get());
        }
    };

    public CompactMachines() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register blocks and items
        Registrations.init();

        // Register the setup method for modloading
        modBus.addListener(this::setup);

        // Register the enqueueIMC method for modloading
        modBus.addListener(this::enqueueIMC);

        // Register the processIMC method for modloading
        modBus.addListener(this::processIMC);

        // Register the doClientStuff method for modloading
        modBus.addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.initialize();

        // Pre-Initialize Code
        // ConfigurationHandler.init(event.getSuggestedConfigurationFile());
//        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
//
//        MinecraftForge.EVENT_BUS.register(MachineEventHandler.class);
//        MinecraftForge.EVENT_BUS.register(PlayerEventHandler.class);
//        MinecraftForge.EVENT_BUS.register(SkyWorldSavedData.class);
//        MinecraftForge.EVENT_BUS.register(WorldSavedDataMachines.class);
//        MinecraftForge.EVENT_BUS.register(RenderTickCounter.class);
//        MinecraftForge.EVENT_BUS.register(BakeryHandler.class);
//        MinecraftForge.EVENT_BUS.register(SkyWorldEvents.class);
//
//        // Insist on keeping an already registered dimension by registering in pre-registerDimension.
//        DimensionTools.registerDimension();
//
//        GuiHandler.init();
//
//        AnnotatedInstanceUtil.setAsmData(event.getAsmData());
//
//        proxy.preInit(event);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        // InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        // TODO: Register compact machines command here?
        // event.registerCommand(new Commandcompactmachines());
    }

//    @EventHandler
//    public void init(FMLInitializationEvent event) {
//        PackageHandler.init();
//
//        proxy.init(event);
//
//        SkyDimension.init();
//
//        MultiblockRecipes.init();
//        SchemaRegistry.init();
//    }
//
//    @EventHandler
//    public void postInit(FMLPostInitializationEvent event) {
//        CapabilityNullHandlerRegistry.registerNullHandlers();
//        ExtraTileDataProviderRegistry.registerExtraTileDataProviders();
//
//        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingMachines());
//
//        proxy.postInit(event);
//    }
//
//    @EventHandler
//    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
//        for(WorldServer world : event.getServer().worlds) {
//            if(world.getChunkProvider().chunkGenerator instanceof SkyChunkGenerator) {
//                world.setSpawnPoint(new BlockPos(0,0,0));
//            }
//        }
//    }
}
