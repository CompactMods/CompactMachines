package com.robotgryphon.compactmachines;

import com.robotgryphon.compactmachines.compat.theoneprobe.TheOneProbeCompat;
import com.robotgryphon.compactmachines.config.CommonConfig;
import com.robotgryphon.compactmachines.config.EnableVanillaRecipesConfigCondition;
import com.robotgryphon.compactmachines.config.ServerConfig;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactMachines.MOD_ID)
public class CompactMachines
{
    public static final String MOD_ID = "compactmachines";

    public static final Logger LOGGER = LogManager.getLogger();

//    public static ClientWorldData clientWorldData;
//    public static final CreativeTabcompactmachines CREATIVE_TAB = new CreativeTabcompactmachines();

    public static ItemGroup COMPACT_MACHINES_ITEMS = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registration.MACHINE_BLOCK_ITEM_NORMAL.get());
        }
    };

    public CompactMachines() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register blocks and items
        Registration.init();

        // Register the setup method for modloading
        modBus.addListener(this::setup);

        // Register the enqueueIMC method for modloading
        modBus.addListener(this::enqueueIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CraftingHelper.register(EnableVanillaRecipesConfigCondition.Serializer.INSTANCE);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.initialize();
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        if(ModList.get().isLoaded("theoneprobe"))
            TheOneProbeCompat.sendIMC();
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
