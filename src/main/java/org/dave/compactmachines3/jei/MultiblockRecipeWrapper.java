package org.dave.compactmachines3.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.misc.RenderTickCounter;
import org.dave.compactmachines3.render.RecipeRenderManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class MultiblockRecipeWrapper implements IRecipeWrapper {
    public final MultiblockRecipe recipe;
    private final List<ItemStack> input = new ArrayList<>();

    public MultiblockRecipeWrapper(MultiblockRecipe recipe) {
        this.recipe = recipe;

        int added = 0;
        for(ItemStack stack : this.recipe.getRequiredItemStacks()) {
            this.input.add(stack);
            added++;
        }

        for(int emptySlot = 0; emptySlot < 6 - added; emptySlot++) {
            this.input.add(null);
        }

        this.input.add(this.recipe.getCatalystStack());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(ItemStack.class, input);
        ingredients.setOutput(ItemStack.class, this.recipe.getTargetStack());
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0F, 0F, 216.5F);

        mc.fontRenderer.drawString(recipe.getDimensionsString(), 153-mc.fontRenderer.getStringWidth(recipe.getDimensionsString()), 19 * 5 + 10, 0x444444);

        GlStateManager.popMatrix();

        float angle = RenderTickCounter.renderTicks * 45.0f / 128.0f;
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
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
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        GlStateManager.pushMatrix();

        // Center on recipe area
        GlStateManager.translate((float)(recipeWidth / 2), (float)(recipeHeight / 2), 100.0f);

        // Shift it a bit down so one can properly see 3d
        GlStateManager.rotate(-25.0f, 1.0f, 0.0f, 0.0f);

        // Rotate per our calculated time
        GlStateManager.rotate(angle, 0.0f, 1.0f, 0.0f);

        // Scale down to gui scale
        GlStateManager.scale(16.0f, -16.0f, 16.0f);

        // Calculate the maximum size the shape has
        BlockPos mn = recipe.getMinPos();
        BlockPos mx = recipe.getMaxPos();
        int diffX = mx.getX() - mn.getX();
        int diffY = mx.getY() - mn.getY();
        int diffZ = mx.getZ() - mn.getZ();

        // We have big recipes, we need to adjust the size accordingly.
        int maxDiff = Math.max(Math.max(diffZ, diffX), diffY) + 1;
        float scale = 1.0f / ((float)maxDiff / 4.0f);

        GlStateManager.enableCull();
        GlStateManager.scale(scale, scale, scale);

        // Move the shape to the center of the crafting window
        GlStateManager.translate(
                (diffX + 1) / -2.0f,
                (diffY + 1) / -2.0f,
                (diffZ + 1) / -2.0f
        );

        // If the client holds down the shift button, render everything as wireframe
        boolean renderWireframe = false;
        if(GuiScreen.isShiftKeyDown()) {
            renderWireframe = true;
            GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        }

        RecipeRenderManager.instance.renderRecipe(recipe, 0.0f);

        if(renderWireframe) {
            GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }

        GlStateManager.popMatrix();
    }
}
