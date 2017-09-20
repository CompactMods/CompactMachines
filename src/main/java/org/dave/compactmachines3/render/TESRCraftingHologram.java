package org.dave.compactmachines3.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.tile.TileEntityCraftingHologram;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TESRCraftingHologram extends TileEntitySpecialRenderer<TileEntityCraftingHologram> {
    private IBlockAccess blockAccess;
    private MultiblockRecipe recipe;

    @Override
    public void render(TileEntityCraftingHologram te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        recipe = te.getRecipe();
        if(recipe == null) {
            return;
        }

        List<BlockPos> toRender = recipe.getShapeAsBlockPosList();
        if(toRender.isEmpty()) {
            return;
        }

        blockAccess = recipe.getBlockAccess();

        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        // Init GlStateManager
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();

        GlStateManager.enableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();

        float angle = RenderTickCounter.renderTicks * 45.0f / 64.0f;


        float rotateOffsetX = (float)recipe.getWidth() / 2.0f;
        float rotateOffsetY = 0.0f;
        float rotateOffsetZ = (float)recipe.getDepth() / 2.0f;

        GlStateManager.translate(0.5f, 0.0f, 0.5f);


        double progress = 1.0d - ((double)te.getProgress() / (double)recipe.getTicks());

        double scale = progress * (1.0f - ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / 1000) + 1.0f) * 0.1f));
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.translate(-rotateOffsetX, -rotateOffsetY, -rotateOffsetZ);

        GlStateManager.translate(rotateOffsetX, rotateOffsetY, rotateOffsetZ);
        GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(-rotateOffsetX, -rotateOffsetY, -rotateOffsetZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // Aaaand render
        buffer.begin(7, DefaultVertexFormats.BLOCK);
        GlStateManager.disableAlpha();
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.SOLID, toRender);
        GlStateManager.enableAlpha();
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT_MIPPED, toRender);
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.CUTOUT, toRender);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        this.renderLayer(blockrendererdispatcher, buffer, BlockRenderLayer.TRANSLUCENT, toRender);

        tessellator.draw();

        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void renderLayer(BlockRendererDispatcher blockrendererdispatcher, BufferBuilder buffer, BlockRenderLayer renderLayer, List<BlockPos> toRender) {
        for (BlockPos pos : toRender) {
            IBlockState state = recipe.getStateAtBlockPos(pos);
            if (!state.getBlock().canRenderInLayer(state, renderLayer)) {
                continue;
            }

            ForgeHooksClient.setRenderLayer(renderLayer);
            try {
                blockrendererdispatcher.renderBlock(state, pos, blockAccess, buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ForgeHooksClient.setRenderLayer(null);
        }
    }
}
