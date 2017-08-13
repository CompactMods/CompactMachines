package org.dave.compactmachines3.proxy;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Itemss;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        Blockss.initModels();
        Itemss.initModels();

        Item item = Item.getItemFromBlock(Blockss.fieldProjector);
        ModelResourceLocation model = new ModelResourceLocation("compactmachines3:fieldprojectorcombined", "inventory");
        ModelBakery.registerItemVariants(item, model);

        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return model;
            }
        });
    }
}
