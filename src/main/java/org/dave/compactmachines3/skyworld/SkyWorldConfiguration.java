package org.dave.compactmachines3.skyworld;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;

import java.util.Collection;

public class SkyWorldConfiguration {
    public Schema schema;
    public boolean startLocked;

    public SkyWorldConfiguration() {
        Collection<Schema> allSchemas = SchemaRegistry.instance.getSchemas();
        this.schema = allSchemas.stream().findFirst().orElse(null);
        startLocked = true;
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
    }

    public String getAsJsonString() {
        JsonObject rootObject = new JsonObject();

        if(schema != null) {
            rootObject.add("schema", new JsonPrimitive(schema.getName()));
        }

        rootObject.add("startLocked", new JsonPrimitive(startLocked));

        return rootObject.toString();
    }
}
