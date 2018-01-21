package org.dave.compactmachines3.miniaturization;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.dave.compactmachines3.utility.Logz;

import java.lang.reflect.Type;
import java.util.Map;

public class MultiblockRecipeSerializer implements JsonSerializer<MultiblockRecipe>, JsonDeserializer<MultiblockRecipe> {
    @Override
    public MultiblockRecipe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            Logz.info("Invalid recipe! Not a json object!");
            return null;
        }

        JsonObject jsonRoot = json.getAsJsonObject();
        // Abort if these sections don't exist
        if(!jsonRoot.has("input-types") || !jsonRoot.has("shape")) {
            Logz.info("Invalid recipe! Missing section shape and/or input-types!");
            return null;
        }

        if(!jsonRoot.has("name")) {
            Logz.info("Invalid recipe! Missing recipe name!");
            return null;
        }

        String name = jsonRoot.get("name").getAsString();
        if(MultiblockRecipes.getRecipeByName(name) != null) {
            Logz.info("Duplicate recipe with name: %s", name);
            return null;
        }

        if(jsonRoot.has("disabled") && jsonRoot.get("disabled").getAsBoolean() == true) {
            Logz.info("Recipe '%s' is disabled via its json file", name);
            return null;
        }

        // Verify we've got a valid target itemstack
        ItemStack targetStack = null;
        int targetCount = jsonRoot.has("target-count") ? jsonRoot.get("target-count").getAsInt() : 1;
        int targetMeta  = jsonRoot.has("target-meta") ? jsonRoot.get("target-meta").getAsInt() : 0;
        if(jsonRoot.has("target-block")) {
            String blockId = jsonRoot.get("target-block").getAsString();
            Block targetBlock = Block.REGISTRY.getObject(new ResourceLocation(blockId));
            if(targetBlock == null) {
                throw new RuntimeException("Invalid recipe! Unknown target block: \""+ blockId +"\"");
            }
            targetStack = new ItemStack(targetBlock, targetCount, targetMeta);
        } else if(jsonRoot.has("target-item")) {
            String itemId = jsonRoot.get("target-item").getAsString();
            Item targetItem = Item.REGISTRY.getObject(new ResourceLocation(itemId));
            if(targetItem == null) {
                throw new RuntimeException("Invalid recipe! Unknown target item: \""+ itemId +"\"");
            }
            targetStack = new ItemStack(targetItem, targetCount, targetMeta);
        }

        if(targetStack.isEmpty()) {
            return null;
        }

        Item catalystItem = Items.REDSTONE;
        if(jsonRoot.has("catalyst")) {
            String itemId = jsonRoot.get("catalyst").getAsString();
            catalystItem = Item.REGISTRY.getObject(new ResourceLocation(itemId));
            if(catalystItem == null) {
                throw new RuntimeException("Invalid recipe! Unknown target item: \""+ itemId +"\"");
            }
        }

        int catalystMeta = 0;
        if(jsonRoot.has("catalyst-meta")) {
            catalystMeta = jsonRoot.get("catalyst-meta").getAsInt();
        }

        NBTTagCompound catalystNbt = null;
        if(jsonRoot.has("catalyst-nbt")) {
            String nbtRaw = jsonRoot.get("catalyst-nbt").getAsString();
            try {
                catalystNbt = JsonToNBT.getTagFromJson(nbtRaw);
            } catch (NBTException e) {
                Logz.warn("Unable to read NBT tag from miniaturiazation recipe: %s (exception=%s)", nbtRaw, e);
            }
        }

        boolean symmetrical = false;
        if(jsonRoot.has("symmetrical")) {
            symmetrical = jsonRoot.get("symmetrical").getAsBoolean();
        }

        int ticks = 100;
        if(jsonRoot.has("duration")) {
            ticks = jsonRoot.get("duration").getAsInt();
        }

        MultiblockRecipe result = new MultiblockRecipe(name, targetStack, catalystItem, catalystMeta, catalystNbt, symmetrical, ticks);

        // Read reference map
        JsonObject jsonReferenceMap = jsonRoot.get("input-types").getAsJsonObject();
        for(Map.Entry<String, JsonElement> entry : jsonReferenceMap.entrySet()) {
            JsonObject data = entry.getValue().getAsJsonObject();
            if(!data.has("id")) {
                Logz.error("Missing id for source block");
                return null;
            }

            String blockId = data.get("id").getAsString();
            Block sourceBlock = Block.REGISTRY.getObject(new ResourceLocation(blockId));
            if(sourceBlock == null) {
                throw new RuntimeException("Invalid recipe! Unknown source block: \""+ blockId +"\"");
            }

            int meta = data.has("meta") ? data.get("meta").getAsInt() : 0;
            IBlockState state = sourceBlock.getStateFromMeta(meta);
            if(state == null) {
                state = sourceBlock.getDefaultState();
            }

            result.addBlockReference(entry.getKey(), state);

            boolean ignoreMeta = data.has("ignore-meta") && data.get("ignore-meta").getAsBoolean();
            if(ignoreMeta) {
                result.setIgnoreMeta(entry.getKey());
            }
        }

        // Read variant map
        if(jsonRoot.has("input-nbt")) {
            JsonObject jsonVariantMap = jsonRoot.get("input-nbt").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonVariantMap.entrySet()) {
                JsonObject data = entry.getValue().getAsJsonObject();
                if (!data.has("nbt")) {
                    Logz.error("Missing nbt for variant");
                    return null;
                }

                String rawNbtJson = data.get("nbt").getAsString();
                try {
                    catalystNbt = JsonToNBT.getTagFromJson(rawNbtJson);
                    result.addBlockVariation(entry.getKey(), catalystNbt);
                } catch (NBTException e) {
                    Logz.warn("Unable to read NBT tag from miniaturiazation recipe: %s (exception=%s)", rawNbtJson, e);
                    return null;
                }
            }
        }

        // Determine dimensions of the shape
        JsonArray jsonYPosArray = jsonRoot.get("shape").getAsJsonArray();
        int height = jsonYPosArray.size();
        int width = 0;
        int depth = 0;
        for (JsonElement jsonYElement : jsonYPosArray) {
            JsonArray jsonZPosArray = jsonYElement.getAsJsonArray();
            depth = Math.max(depth, jsonZPosArray.size());

            for(JsonElement jsonZElement : jsonZPosArray) {
                JsonArray jsonXPosArray = jsonZElement.getAsJsonArray();
                width = Math.max(width, jsonXPosArray.size());
            }
        }

        // Read position data
        String[][][] positionMap = new String[height][depth][width];
        String[][][] variantMap = new String[height][depth][width];
        int y = 0;
        for (JsonElement jsonYElement : jsonYPosArray) {
            int z = 0;
            for(JsonElement jsonZElement : jsonYElement.getAsJsonArray()) {
                int x = 0;
                for(JsonElement jsonXElement : jsonZElement.getAsJsonArray()) {
                    String ref = jsonXElement.getAsString();
                    if(ref.contains(":")) {
                        positionMap[height - 1 - y][z][x] = ref.substring(0, ref.indexOf(':'));
                        variantMap[height - 1 - y][z][x] = ref;
                    } else {
                        positionMap[height - 1 - y][z][x] = ref;
                    }
                    x++;
                }
                z++;
            }
            y++;
        }

        result.setPositionMap(positionMap);
        result.setVariantMap(variantMap);

        return result;
    }

    @Override
    public JsonElement serialize(MultiblockRecipe src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        return root;
    }
}
