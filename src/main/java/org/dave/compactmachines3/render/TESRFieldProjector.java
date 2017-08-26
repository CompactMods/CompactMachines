package org.dave.compactmachines3.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.block.BlockFieldProjector;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.utility.Logz;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TESRFieldProjector extends TileEntitySpecialRenderer<TileEntityFieldProjector> {
    private IModel model;
    private IBakedModel bakedModel;

    private IBakedModel getBakedModel() {
        if(bakedModel == null) {
            try {
                model = ModelLoaderRegistry.getModel(new ResourceLocation(CompactMachines3.MODID, "block/fieldprojectordish"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("compactmachines3:blocks/fieldprojector"));
        }

        return bakedModel;
    }

    @Override
    public void render(TileEntityFieldProjector te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();

        // Offset the dish a bit, so we can rotate around 0/0/0
        GlStateManager.translate(0.5, 0.0, 0.5);

        // Rotate to face the proper direction
        EnumFacing facing = EnumFacing.NORTH;
        if(te.getWorld().getBlockState(te.getPos()).getBlock() == Blockss.fieldProjector) {
            facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockFieldProjector.FACING);
        }

        int xyAngle = (facing.getHorizontalIndex()-1) * -90;
        GlStateManager.rotate(xyAngle, 0, 1, 0);

        // Create a up/down-swinging animation
        double zAngle = Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / 5000)*10;

        float yDiskOffset = -0.66f;
        GlStateManager.translate(0.0, -yDiskOffset, 0.0);
        GlStateManager.rotate((float)zAngle, 0, 0, 1);
        GlStateManager.translate(0.0, yDiskOffset, 0.0);

        // Center the dish in the block
        GlStateManager.translate(-0.5, 0.0, -0.5);

        RenderHelper.disableStandardItemLighting();

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        World world = te.getWorld();

        GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                world,
                getBakedModel(),
                world.getBlockState(te.getPos()),
                te.getPos(),
                Tessellator.getInstance().getBuffer(),
                false);

        tessellator.draw();
        GlStateManager.popMatrix();

        RenderHelper.enableStandardItemLighting();

        GlStateManager.popAttrib();
    }
}
