package org.dave.compactmachines3.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        //registerFluidModels();
    }

    /*
    public static void registerFluidModels() {
        Item fluidBlockItem = Item.getItemFromBlock(Blockss.miniaturizationFluidBlock);
        if(fluidBlockItem == null) {
            return;
        }

        ModelBakery.registerItemVariants(fluidBlockItem);

        final ModelResourceLocation modelResourceLocation = new ModelResourceLocation("compactmachines3:fluid", "miniaturization_fluid_block");
        ModelLoader.setCustomMeshDefinition(fluidBlockItem, new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return modelResourceLocation;
            }
        });

        ModelLoader.setCustomStateMapper(Blockss.miniaturizationFluidBlock, new StateMapperBase() {
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return modelResourceLocation;
            }
        });
    }

    private void registerItemBlockRenderer(Block block) {
        Item item = Item.getItemFromBlock(block);
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private void registerMetaItemBlockRenderers() {
        ItemModelMesher imm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        Item itemBlockMachine = Item.getItemFromBlock(Blockss.machine);
        for(EnumMachineSize size : EnumMachineSize.values()) {
            imm.register(itemBlockMachine, size.getMeta(), new ModelResourceLocation(itemBlockMachine.getRegistryName(), "size=" + size.getName()));
        }


    }
    */


}
