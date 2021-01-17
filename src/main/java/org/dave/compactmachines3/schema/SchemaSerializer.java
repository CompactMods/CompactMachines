package org.dave.compactmachines3.schema;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.math.Vec3d;
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

        if(jsonRoot.has("description")) {
            result.setDescription(jsonRoot.get("description").getAsString());
        }

        JsonArray spawnPos = jsonRoot.getAsJsonArray("spawn");
        Vec3d spawnPosition = new Vec3d(
                spawnPos.get(0).getAsDouble(),
                spawnPos.get(1).getAsDouble(),
                spawnPos.get(2).getAsDouble());
        result.setSpawnPosition(spawnPosition);

        return result;
    }

    @Override
    public JsonElement serialize(Schema src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("name", src.getName());
        root.addProperty("description", src.getDescription());
        root.addProperty("size", src.getSize().getMeta());
        root.add("blocks", context.serialize(src.getBlocks()));

        JsonArray spawnArray = new JsonArray();
        spawnArray.add(src.getSpawnPosition().x);
        spawnArray.add(src.getSpawnPosition().y);
        spawnArray.add(src.getSpawnPosition().z);

        root.add("spawn", spawnArray);
        return root;
    }
}
