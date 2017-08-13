package org.dave.compactmachines3.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.dave.compactmachines3.CompactMachines3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MultiblockRecipeCategory implements IRecipeCategory, ITooltipCallback<ItemStack> {
    public static final String UID = CompactMachines3.MODID + ".MultiblockMiniaturization";
    private final String localizedName;
    private final IDrawableStatic background;
    private final IDrawableStatic slotDrawable;

    public MultiblockRecipeCategory(IGuiHelper guiHelper) {
        localizedName = I18n.format("compactmachines3.jei.category.multiblock_miniaturization");
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

    /**
     * Return the name of the mod associated with this recipe category.
     * Used for the recipe category tab's tooltip.
     *
     * @since JEI 4.5.0
     */
    @Override
    public String getModName() {
        return CompactMachines3.MODID;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    /**
     * Optional icon for the category tab.
     * If no icon is defined here, JEI will use first item registered with {@link IModRegistry#addRecipeCatalyst(Object, String...)}
     *
     * @return icon to draw on the category tab, max size is 16x16 pixels.
     * @since 3.13.1
     */
    @Nullable
    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slotDrawable.draw(minecraft, 0, 19 * 0);
        slotDrawable.draw(minecraft, 0, 19 * 1);
        slotDrawable.draw(minecraft, 0, 19 * 2);
        slotDrawable.draw(minecraft, 0, 19 * 3);
        slotDrawable.draw(minecraft, 0, 19 * 4);
        slotDrawable.draw(minecraft, 0, 19 * 5);

        slotDrawable.draw(minecraft, 135, 19 * 4);
        //slotDrawable.draw(minecraft, 135, 19 * 5);

        slotDrawable.draw(minecraft, 135, 0);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 19 * 0);
        recipeLayout.getItemStacks().init(1, true, 0, 19 * 1);
        recipeLayout.getItemStacks().init(2, true, 0, 19 * 2);
        recipeLayout.getItemStacks().init(3, true, 0, 19 * 3);
        recipeLayout.getItemStacks().init(4, true, 0, 19 * 4);
        recipeLayout.getItemStacks().init(5, true, 0, 19 * 5);

        recipeLayout.getItemStacks().init(6, true, 135, 19 * 4);
        recipeLayout.getItemStacks().init(7, true, 135, 19 * 5);

        recipeLayout.getItemStacks().init(8, false, 135, 0);

        recipeLayout.getItemStacks().addTooltipCallback(this);
        recipeLayout.getItemStacks().set(ingredients);
    }

    /**
     * Get the tooltip for whatever's under the mouse.
     * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
     * <p>
     * To add to ingredient tooltips, see {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}
     * To add tooltips for a recipe wrapper, see {@link IRecipeWrapper#getTooltipStrings(int, int)}
     *
     * @param mouseX the X position of the mouse, relative to the recipe.
     * @param mouseY the Y position of the mouse, relative to the recipe.
     * @return tooltip strings. If there is no tooltip at this position, return an empty list.
     * @since JEI 4.2.5
     */
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Collections.emptyList();
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        String last = tooltip.get(tooltip.size()-1);
        tooltip.remove(tooltip.size()-1);
        if(slotIndex >= 0 && slotIndex <= 5) {
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.compactmachines3.jei.shape"));
        }
        if(slotIndex == 7) {
            tooltip.add(TextFormatting.YELLOW + I18n.format("tooltip.compactmachines3.jei.crafting_trigger"));
        }
        tooltip.add(last);
    }
}
