package pneumaticCraft.api.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import pneumaticCraft.api.PneumaticRegistry;

/**
 * @Deprecated Access via {@link pneumaticCraft.api.recipe.IPneumaticRecipeRegistry}
 */
@Deprecated
public class AssemblyRecipe{

    public static List<AssemblyRecipe> drillRecipes = new ArrayList<AssemblyRecipe>();
    public static List<AssemblyRecipe> laserRecipes = new ArrayList<AssemblyRecipe>();
    public static List<AssemblyRecipe> drillLaserRecipes = new ArrayList<AssemblyRecipe>();

    private final ItemStack input;
    private final ItemStack output;

    public AssemblyRecipe(ItemStack input, ItemStack output){
        this.input = input;
        this.output = output;
    }

    public ItemStack getInput(){
        return input;
    }

    public ItemStack getOutput(){
        return output;
    }

    public static void addDrillRecipe(Object input, Object output){
        PneumaticRegistry.getInstance().getRecipeRegistry().addAssemblyDrillRecipe(input, output);
    }

    public static void addLaserRecipe(Object input, Object output){
        PneumaticRegistry.getInstance().getRecipeRegistry().addAssemblyLaserRecipe(input, output);
    }
}
