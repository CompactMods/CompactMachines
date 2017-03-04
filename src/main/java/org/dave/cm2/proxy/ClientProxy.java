package org.dave.cm2.proxy;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Itemss;
import org.dave.cm2.reference.EnumMachineSize;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        registerItemRenderer(Itemss.psd);
        registerItemRenderer(Itemss.tunnelTool);
        registerFluidModels();
    }

    private void registerItemRenderer(Item item) {
        ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(item.getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, itemModelResourceLocation);
    }

    public static void registerFluidModels() {
        Item fluidBlockItem = Item.getItemFromBlock(Blockss.miniaturizationFluidBlock);
        if(fluidBlockItem == null) {
            return;
        }

        ModelBakery.registerItemVariants(fluidBlockItem);

        final ModelResourceLocation modelResourceLocation = new ModelResourceLocation("cm2:fluid", "miniaturization_fluid_block");
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

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        registerItemBlockRenderer(Blockss.tunnel);
        registerItemBlockRenderer(Blockss.wall);
        registerItemBlockRenderer(Blockss.wallBreakable);

        registerMetaItemBlockRenderers();
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


}
