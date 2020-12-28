package org.dave.compactmachines3.skyworld;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;

import java.util.Arrays;
import java.util.Collection;

public class SkyWorldConfiguration {
    public Schema schema;
    public boolean startLocked;
    public boolean givePSD;

    public EnumSkyWorldSize size;

    public SkyWorldConfiguration() {
        Collection<Schema> allSchemas = SchemaRegistry.instance.getSchemas();
        this.schema = allSchemas.stream().findFirst().orElse(null);
        startLocked = true;
        givePSD = true;
        size = EnumSkyWorldSize.SMALL;
    }

    public SkyWorldConfiguration(String chunkProviderSettingsJson) {
        this();

        JsonParser myParser = new JsonParser();
        JsonElement rootElement = myParser.parse(chunkProviderSettingsJson);

        if(!rootElement.isJsonObject()) {
            return;
        }

        JsonObject rootObject = rootElement.getAsJsonObject();
        if(rootObject.has("startLocked")) {
            this.startLocked = rootObject.get("startLocked").getAsBoolean();
        }

        if(rootObject.has("schema")) {
            String schemaName = rootObject.get("schema").getAsString();
            if(SchemaRegistry.instance.hasSchema(schemaName)) {
                this.schema = SchemaRegistry.instance.getSchema(schemaName);
            }
        }

        if(rootObject.has("givePSD")) {
            this.givePSD = rootObject.get("givePSD").getAsBoolean();
        }

        if(rootObject.has("size")) {
            String size = rootObject.get("size").getAsString();
            if(!Arrays.stream(EnumSkyWorldSize.values()).anyMatch(s -> s.name().equals(size))) {
                CompactMachines3.logger.warn("Invalid size value specified: {}. Falling back to SMALL.", size);
                this.size = EnumSkyWorldSize.SMALL;
            } else {
                this.size = EnumSkyWorldSize.valueOf(size);
            }
        }
    }

    public String getAsJsonString() {
        JsonObject rootObject = new JsonObject();

        if(schema != null) {
            rootObject.add("schema", new JsonPrimitive(schema.getName()));
        }

        rootObject.add("startLocked", new JsonPrimitive(startLocked));
        rootObject.add("givePSD", new JsonPrimitive(givePSD));
        rootObject.add("size", new JsonPrimitive(size.name()));

        return rootObject.toString();
    }
}
