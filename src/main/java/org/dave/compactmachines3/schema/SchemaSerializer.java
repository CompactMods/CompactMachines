package org.dave.compactmachines3.schema;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

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
        return new Schema(jsonRoot.get("name").getAsString(), context.deserialize(jsonRoot.get("blocks").getAsJsonArray(), type));
    }

    @Override
    public JsonElement serialize(Schema src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("name", src.name);
        root.add("blocks", context.serialize(src.blocks));
        return root;
    }
}
