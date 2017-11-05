package org.dave.compactmachines3.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
import org.lwjgl.opengl.GL11;

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

    private void renderModel(TileEntityFieldProjector te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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

    private void renderField(TileEntityFieldProjector te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        BlockPos centerOfField = te.getPos().offset(te.getDirection(), te.getActiveMagnitude()*2);
        AxisAlignedBB cube = new AxisAlignedBB(centerOfField).grow(te.getActiveMagnitude()-0.98d);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.translate(x, y, z);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.glLineWidth(1f);
        GlStateManager.color(1f, 1f, 1f);

        double extraLength = 0.0d;
        cube = cube.offset(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
        if(te.getActiveRecipe() != null && te.getCraftingProgress() > 0) {
            double progress = (1.0d - ((double)te.getCraftingProgress() / (double)te.getActiveRecipe().getTicks()));
            double scale = 1.0d - (progress * (1.0f - ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / 4000) + 1.0f) * 0.1f)));
            scale = Math.min(scale, 0.9d);
            cube = cube.shrink(scale * te.getActiveMagnitude());
            extraLength = scale * te.getActiveMagnitude() * 2;
        }
        renderOutline(cube, te);

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);

        renderFaces(cube, extraLength);

        GlStateManager.depthMask(true);
        GlStateManager.glLineWidth(1f);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        GlStateManager.enableTexture2D();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private void renderFaces(AxisAlignedBB cube, double extraLength) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int color = 0xFF6A00;
        float cR = (color >> 16 & 255) / 255.0f;
        float cG = (color >> 8 & 255) / 255.0f;
        float cB = (color & 255) / 255.0f;
        float cA = 0.15f;

        double x1 = cube.minX;
        double x2 = cube.maxX;
        double y1 = cube.minY;
        double y2 = cube.maxY;
        double z1 = cube.minZ;
        double z2 = cube.maxZ;
        double radius = (cube.maxY - cube.minY) / 2;

        double y4 = cube.maxY - radius + 0.3d;

        // Draw the faces
        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();

        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();

        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();

        // Render projection planes
        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (cube.maxY - cube.minY);
        double y3 = y1 + zAngle;
        float cA2 = 0.105f;

        // Ensure both sides of the plane are visible
        GlStateManager.disableCull();

        // north -> south
        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2-(radius-0.2f), y4, z1-(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x1+(radius-0.2f), y4, z1-(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();

        // east -> west
        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2+(radius+0.8f+extraLength), y4, z2-(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2+(radius+0.8f+extraLength), y4, z1+(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();

        // south -> north
        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x2-(radius-0.2f), y4, z2+(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x1+(radius-0.2f), y4, z2+(radius+0.8f+extraLength)).color(cR, cG, cB, cA2).endVertex();

        // west -> east
        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x1-(radius+0.8f+extraLength), y4, z2-(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();
        buffer.pos(x1-(radius+0.8f+extraLength), y4, z1+(radius-0.2f)).color(cR, cG, cB, cA2).endVertex();


        tessellator.draw();
        GlStateManager.enableCull();
    }

    private void renderOutline(AxisAlignedBB cube, TileEntityFieldProjector te) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        int color = 0xFF6A00;
        float cR = (color >> 16 & 255) / 255.0f;
        float cG = (color >> 8 & 255) / 255.0f;
        float cB = (color & 255) / 255.0f;
        float cA = 0.5f;

        double x1 = cube.minX;
        double x2 = cube.maxX;
        double y1 = cube.minY;
        double y2 = cube.maxY;
        double z1 = cube.minZ;
        double z2 = cube.maxZ;

        /*
        // Draw the actual outline of the cube
        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y2, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y1, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y2, z2).color(cR, cG, cB, cA).endVertex();
        */

        // Draw the up and down bouncing lines on the sides
        double zAngle = ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / -5000) + 1.0d) / 2) * (y2 - y1);
        double y3 = y1 + zAngle;
        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA).endVertex();

        buffer.pos(x1, y3, z1).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA).endVertex();

        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y3, z1).color(cR, cG, cB, cA).endVertex();

        buffer.pos(x1, y3, z2).color(cR, cG, cB, cA).endVertex();
        buffer.pos(x2, y3, z2).color(cR, cG, cB, cA).endVertex();

        tessellator.draw();
    }

    @Override
    public void render(TileEntityFieldProjector te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        renderModel(te, x, y, z, partialTicks, destroyStage, alpha);

        if(te.isMaster() && te.shouldRenderField()) {
            renderField(te, x, y, z, partialTicks, destroyStage, alpha);
        }
    }
}
