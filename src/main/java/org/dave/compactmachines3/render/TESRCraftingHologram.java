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
import org.dave.compactmachines3.world.ProxyWorld;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TESRCraftingHologram extends TileEntitySpecialRenderer<TileEntityCraftingHologram> {
    private IBlockAccess blockAccess;
    private MultiblockRecipe recipe;
    private ProxyWorld proxyWorld;
    private int glListId = -1;

    @Override
    public void render(TileEntityCraftingHologram te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        recipe = te.getRecipe();
        if(recipe == null) {
            return;
        }

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

        double progress = 1.0d - ((double)te.getProgress() / (double)recipe.getTicks());

        double scale = progress * (1.0f - ((Math.sin(Math.toDegrees(RenderTickCounter.renderTicks) / 2000) + 1.0f) * 0.1f));
        scale *= 0.7d;

        GlStateManager.translate(0.5d, 0.5d, 0.5d);

        GlStateManager.scale(scale, scale, scale);

        GlStateManager.translate(0.0f, -recipe.getHeight() / 2 , 0.0f);

        GlStateManager.translate(-rotateOffsetX, -rotateOffsetY, -rotateOffsetZ);

        GlStateManager.translate(rotateOffsetX, rotateOffsetY, rotateOffsetZ);
        GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(-rotateOffsetX, -rotateOffsetY, -rotateOffsetZ);

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.disableBlend();
        RecipeRenderManager.instance.renderRecipe(recipe, partialTicks);

        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();


        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
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
