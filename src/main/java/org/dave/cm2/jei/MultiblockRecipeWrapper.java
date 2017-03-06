package org.dave.cm2.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.UniversalBucket;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.miniaturization.MiniaturizationRecipe;

import java.util.ArrayList;
import java.util.List;

public class MultiblockRecipeWrapper extends BlankRecipeWrapper {
    public final MiniaturizationRecipe recipe;
    private final List<ItemStack> input = new ArrayList<>();
    private int requiredBuckets;

    public MultiblockRecipeWrapper(MiniaturizationRecipe recipe) {
        this.recipe = recipe;
        this.requiredBuckets = (int) Math.ceil(this.recipe.getWidth() / 2.0f);
        this.requiredBuckets *= this.requiredBuckets;

        this.input.add(new ItemStack(this.recipe.getSourceBlock(), this.recipe.getRequiredSourceBlockCount()));
        this.input.add(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid));
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(ItemStack.class, input);
        ingredients.setOutput(ItemStack.class, this.recipe.getTargetStack());
    }

    private void drawBucket(Minecraft mc) {
        ItemStack fluidBucket = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0F, 0.45F, 0.9F);
        GlStateManager.scale(0.5, 0.5, 0.5);

        mc.getRenderItem().renderItem(fluidBucket, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.popMatrix();
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if(requiredBuckets > 1) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0F, 0F, 216.5F);

            if (requiredBuckets < 10) {
                mc.fontRendererObj.drawStringWithShadow("" + requiredBuckets, 12, 28, 0xFFFFFF);
            } else {
                mc.fontRendererObj.drawStringWithShadow("" + requiredBuckets, 6, 28, 0xFFFFFF);
            }

            GlStateManager.popMatrix();
        }

        ItemStack sourceBlock = new ItemStack(this.recipe.getSourceBlock());

        GlStateManager.pushMatrix();
        GlStateManager.translate(75F, 0F, 16.5F);
        GlStateManager.scale(20, -20, 20);

        float totalHeight = Math.max(5, this.recipe.getWidth()) * 0.45F + 0.45F;
        totalHeight += (this.recipe.getHeight()) * 0.45F;

        float hRatio = recipeHeight / (20*totalHeight);
        GlStateManager.scale(hRatio, hRatio, hRatio);

        if(totalHeight < 4) {
            GlStateManager.translate(0, -0.3f * (totalHeight), 0);
        }

        // Top layer, back
        boolean isBucketRow = true;
        for(int stacksToDraw = 1; stacksToDraw <= this.recipe.getWidth(); stacksToDraw++) {
            boolean isBucketCell = true;
            for(int stackNum = 0; stackNum < stacksToDraw; stackNum++) {
                mc.getRenderItem().renderItem(sourceBlock, ItemCameraTransforms.TransformType.GUI);
                if(isBucketRow && isBucketCell) {
                    drawBucket(mc);
                }
                GlStateManager.translate(0.9F, 0F, 0F);
                isBucketCell = !isBucketCell;
            }

            // CR+LF
            GlStateManager.translate((-0.9F * stacksToDraw) - 0.45F, -0.225F, 0F);

            // Next row needs to be a bit more in the front
            GlStateManager.translate(0, 0, 0.45F);
            isBucketRow = !isBucketRow;
        }

        // Top layer, front
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.9F, 0F, 0F);
        for(int stacksToDraw = this.recipe.getWidth()-1; stacksToDraw > 0; stacksToDraw--) {
            boolean isBucketCell = true;
            for(int stackNum = 0; stackNum < stacksToDraw; stackNum++) {
                mc.getRenderItem().renderItem(sourceBlock, ItemCameraTransforms.TransformType.GUI);
                if(isBucketRow && isBucketCell) {
                    drawBucket(mc);
                }
                GlStateManager.translate(0.9F, 0F, 0F);
                isBucketCell = !isBucketCell;
            }

            // CR+LF
            GlStateManager.translate((-0.9F * (stacksToDraw-1) ) - 0.45F, -0.225F, 0F);

            // Next row needs to be a bit more in the front
            GlStateManager.translate(0, 0, 0.45F);
            isBucketRow = !isBucketRow;
        }
        GlStateManager.popMatrix();

        // The remaining layers
        GlStateManager.translate(0.45F, -0.315F, -0.9F);
        for(int layer = 1; layer < this.recipe.getHeight(); layer++) {
            GlStateManager.pushMatrix();

            // Left side
            for(int stackNum = 0; stackNum < this.recipe.getWidth(); stackNum++) {
                mc.getRenderItem().renderItem(sourceBlock, ItemCameraTransforms.TransformType.GUI);
                GlStateManager.translate(0.45F, -0.225F, 0.45F);
            }

            // Right side
            GlStateManager.translate(0, 0.45F, -0.9F);
            for(int stackNum = 0; stackNum < this.recipe.getWidth()-1; stackNum++) {
                mc.getRenderItem().renderItem(sourceBlock, ItemCameraTransforms.TransformType.GUI);
                GlStateManager.translate(0.45F, 0.225F, -0.45F);
            }

            GlStateManager.popMatrix();

            // One layer down
            GlStateManager.translate(0, -0.55F, -0.45F);
        }

        GlStateManager.popMatrix();
    }
}
