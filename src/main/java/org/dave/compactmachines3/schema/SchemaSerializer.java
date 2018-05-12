package org.dave.compactmachines3.schema;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.dave.compactmachines3.reference.EnumMachineSize;

import java.lang.reflect.Type;
import java.util.List;

public class SchemaSerializer implements JsonSerializer<Schema>, JsonDeserializer<Schema> {

    @Override
    public Schema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return null;
        }

        JsonObject jsonRoot = json.getAsJsonObject();
        Type type = new TypeToken<List<BlockInformation>>() {}.getType();
        EnumMachineSize size = EnumMachineSize.getFromMeta(jsonRoot.get("size").getAsInt());
        Schema result = new Schema(jsonRoot.get("name").getAsString());
        result.setBlocks(context.deserialize(jsonRoot.get("blocks").getAsJsonArray(), type));
        result.setSize(size);

        JsonArray spawnPos = jsonRoot.getAsJsonArray("spawn");
        double[] spawnPosition = new double[] {
                spawnPos.get(0).getAsDouble(),
                spawnPos.get(1).getAsDouble(),
                spawnPos.get(2).getAsDouble()
        };
        result.setSpawnPosition(spawnPosition);

        return result;
    }

    @Override
    public JsonElement serialize(Schema src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("name", src.getName());
        root.addProperty("size", src.getSize().getMeta());
        root.add("blocks", context.serialize(src.getBlocks()));

        JsonArray spawnArray = new JsonArray();
        spawnArray.add(src.getSpawnPosition()[0]);
        spawnArray.add(src.getSpawnPosition()[1]);
        spawnArray.add(src.getSpawnPosition()[2]);

        root.add("spawn", spawnArray);
        return root;
    }
}
