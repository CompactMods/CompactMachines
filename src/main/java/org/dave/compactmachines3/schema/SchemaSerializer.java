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
        return new Schema(jsonRoot.get("name").getAsString(), context.deserialize(jsonRoot.get("blocks").getAsJsonArray(), type), size);
    }

    @Override
    public JsonElement serialize(Schema src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("name", src.name);
        root.addProperty("size", src.size.getMeta());
        root.add("blocks", context.serialize(src.blocks));
        return root;
    }
}
