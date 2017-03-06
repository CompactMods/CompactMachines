package org.dave.cm2.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import org.dave.cm2.CompactMachines2;

public class MultiblockRecipeCategory extends BlankRecipeCategory {
    public static final String UID = CompactMachines2.MODID + ".MultiblockMiniaturization";
    private final String localizedName;
    private final IDrawableStatic background;
    private final IDrawableStatic slotDrawable;

    public MultiblockRecipeCategory(IGuiHelper guiHelper) {
        // TODO: Localization
        localizedName = "Multiblock Miniaturization";
        background = guiHelper.createBlankDrawable(150, 110);
        slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slotDrawable.draw(minecraft, 0, 0);
        slotDrawable.draw(minecraft, 0, 19);
        slotDrawable.draw(minecraft, 135, 0);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().init(1, true, 0, 19);
        recipeLayout.getItemStacks().init(2, false, 135, 0);
        recipeLayout.getItemStacks().set(ingredients);
    }
}
