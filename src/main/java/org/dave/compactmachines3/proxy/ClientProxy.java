package org.dave.compactmachines3.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.misc.TextureStitchHandler;
import org.dave.compactmachines3.particles.ParticleBlockMarker;
import org.dave.compactmachines3.world.ClientWorldData;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        Blockss.initModels();
        Itemss.initModels();

        Item item = Item.getItemFromBlock(Blockss.fieldProjector);
        ModelResourceLocation model = new ModelResourceLocation("compactmachines3:fieldprojectorcombined", "inventory");
        ModelBakery.registerItemVariants(item, model);

        ModelLoader.setCustomMeshDefinition(item, stack -> model);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(TextureStitchHandler.class);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        CompactMachines3.clientWorldData = new ClientWorldData();
    }

    @Override
    public void renderBlockMarker(double x, double y, double z) {
        ParticleBlockMarker particle = new ParticleBlockMarker(Minecraft.getMinecraft().world, x, y, z);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }
}
