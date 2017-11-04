package org.dave.compactmachines3.misc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.dave.compactmachines3.utility.Logz;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.minecraftforge.common.ForgeHooks.getContainerItem;

public class ShapedOreReturnableRecipe extends ShapedOreRecipe {
    List<Integer> slotsToReturn;

    public ShapedOreReturnableRecipe(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer, List<Integer> slotsToReturn) {
        super(group, result, primer);
        this.slotsToReturn = slotsToReturn;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < ret.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if(slotsToReturn.contains(i)) {
                ret.set(i, stack.copy());
            } else {
                ret.set(i, getContainerItem(stack));
            }
        }
        return ret;
    }

    public static class Factory implements IRecipeFactory {
        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            String group = JsonUtils.getString(json, "group", "");
            //if (!group.isEmpty() && group.indexOf(':') == -1)
            //    group = context.getModId() + ":" + group;

            List<Ingredient> returnList = new ArrayList<>();
            Map<Character, Ingredient> ingMap = Maps.newHashMap();
            for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet())
            {
                if (entry.getKey().length() != 1)
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
                if (" ".equals(entry.getKey()))
                    throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");

                Ingredient ing = CraftingHelper.getIngredient(entry.getValue(), context);
                ingMap.put(entry.getKey().toCharArray()[0], ing);

                boolean isJsonObject = entry.getValue().isJsonObject();
                boolean hasReturnValue = isJsonObject && entry.getValue().getAsJsonObject().has("return");
                boolean shouldBeReturned = hasReturnValue && entry.getValue().getAsJsonObject().get("return").getAsBoolean();
                if(shouldBeReturned) {
                    returnList.add(ing);
                }
            }

            ingMap.put(' ', Ingredient.EMPTY);

            JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

            if (patternJ.size() == 0)
                throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");

            String[] pattern = new String[patternJ.size()];
            for (int x = 0; x < pattern.length; ++x)
            {
                String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
                if (x > 0 && pattern[0].length() != line.length())
                    throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
                pattern[x] = line;
            }

            CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
            primer.width = pattern[0].length();
            primer.height = pattern.length;
            primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
            primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);

            Set<Character> keys = Sets.newHashSet(ingMap.keySet());
            keys.remove(' ');

            List<Integer> slotsToReturn = new ArrayList<>();
            int x = 0;
            for (String line : pattern)
            {
                for (char chr : line.toCharArray())
                {
                    Ingredient ing = ingMap.get(chr);
                    if (ing == null)
                        throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
                    if(returnList.contains(ing)) {
                        slotsToReturn.add(x);
                    }
                    primer.input.set(x++, ing);
                    keys.remove(chr);
                }
            }

            if (!keys.isEmpty())
                throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);

            ItemStack resultStack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
            ShapedOreReturnableRecipe result = new ShapedOreReturnableRecipe(group.isEmpty() ? null : new ResourceLocation(group), resultStack, primer, slotsToReturn);
            return result;
        }
    }

}
