package pneumaticCraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Interface accessed via PneumaticRegistry.getInstance().getRecipeRegistry(), used to register recipes to PneumaticCraft.
 * @author MineMaarten
 */
public interface IPneumaticRecipeRegistry{
    /**
     * 
     * @param requiredFluid can be null
     * @param requiredItem can be null
     * @param requiredTemperature in degrees Kelvin.
     * @param requiredPressure required pressure.
     * @param output the output of the recipe. cannot be null.
     */
    public void registerThermopneumaticProcessingPlantRecipe(FluidStack requiredFluid, ItemStack requiredItem, FluidStack output, double requiredTemperature, float requiredPressure);

    /**
     * Allows for registry of a recipe which allows for all your custom needs.
     * @param recipe
     */
    public void registerThermopneumaticProcessingPlantRecipe(IThermopneumaticProcessingPlantRecipe recipe);

    /**
     * Add a recipe that needs an item to be drilled in an Assembly set-up to get the output.
     * @param input valid types: Block, Item, ItemStack
     * @param output valid types: Block, Item, ItemStack
     */
    public void addAssemblyDrillRecipe(Object input, Object output);

    /**
     * Add a recipe that needs an item to be lasered in an Assembly set-up to get the output.
     * @param input valid types: Block, Item, ItemStack
     * @param output valid types: Block, Item, ItemStack
     */
    public void addAssemblyLaserRecipe(Object input, Object output);

    /**
     * Adds a recipe to the Pressure Chamber.
     * @param input , either of type ItemStack or Pair<String, Integer>, where the String is the Ore dictionary entry and Integer is the amount (stack size)
     * @param pressureRequired negative pressures for negative pressure needs.
     * @param output
     */
    public void registerPressureChamberRecipe(Object[] input, float pressureRequired, ItemStack[] output);

    /**
     * Allows for registry of a recipe which allows for all your custom needs.
     * @param recipe
     */
    public void registerPressureChamberRecipe(IPressureChamberRecipe recipe);

    /**
     * Adds an Amadron offer. Both the input and output can either be ItemStack or FluidStack. An exception will be thrown if this is not the case.
     * This is a default offer, meaning it will be put in a clean config load. After that the user can change it at will to remove this added recipe.
     * It's a static offer, meaning if it exists in the instance, it will be there forever (like the Emerald --> PCB Blueprint offer).
     * @param input
     * @param output
     */
    public void registerDefaultStaticAmadronOffer(Object input, Object output);

    /**
     * Adds an Amadron offer. Both the input and output can either be ItemStack or FluidStack. An exception will be thrown if this is not the case.
     * This is a default offer, meaning it will be put in a clean config load. After that the user can change it at will to remove this added recipe.
     * It's a periodic offer, meaning it will be shuffled (by default) once per day between other periodic offers, like the villager trade offers.
     * @param input
     * @param output
     */
    void registerDefaultPeriodicAmadronOffer(Object input, Object output);

    /**
     * Adds a behaviour for when an inventory is framed with a Heat Frame, and is cooled below 0 degrees C. If the input item is a container item it will be returned as well.
     * @param input either of type ItemStack or Pair<String, Integer>, where the String is the Ore dictionary entry and Integer is the amount (stack size)
     * @param output
     */
    public void registerHeatFrameCoolRecipe(Object input, ItemStack output);

}
