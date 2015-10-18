package pneumaticCraft.api.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @Deprecated Access via {@link pneumaticCraft.api.recipe.IPneumaticRecipeRegistry}
 */
@Deprecated
public class PressureChamberRecipe{
    public static List<PressureChamberRecipe> chamberRecipes = new ArrayList<PressureChamberRecipe>();
    public static List<IPressureChamberRecipe> specialRecipes = new ArrayList<IPressureChamberRecipe>();

    public final Object[] input;
    public final ItemStack[] output;
    public final float pressure;

    @Deprecated
    public PressureChamberRecipe(ItemStack[] input, float pressureRequired, ItemStack[] output, boolean outputAsBlock){
        this.input = input;
        this.output = output;
        pressure = pressureRequired;
    }

    public PressureChamberRecipe(Object[] input, float pressureRequired, ItemStack[] output){
        for(Object o : input) {
            if(!(o instanceof ItemStack) && !(o instanceof Pair)) throw new IllegalArgumentException("Input objects need to be of type ItemStack or (Apache's) Pair<String, Integer>. Violating object: " + o);
            if(o instanceof Pair) {
                Pair pair = (Pair)o;
                if(!(pair.getKey() instanceof String)) throw new IllegalArgumentException("Pair key needs to be a String (ore dict entry)");
                if(!(pair.getValue() instanceof Integer)) throw new IllegalArgumentException("Value key needs to be an Integer (amount)");
            }
        }
        this.input = input;
        this.output = output;
        pressure = pressureRequired;
    }
}
