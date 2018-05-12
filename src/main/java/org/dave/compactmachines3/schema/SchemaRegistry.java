package org.dave.compactmachines3.schema;

import com.google.gson.stream.JsonReader;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.utility.ResourceLoader;
import org.dave.compactmachines3.utility.SerializationHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaRegistry {
    public static SchemaRegistry instance;

    public static void init() {
        SchemaRegistry.instance = new SchemaRegistry();
    }


    private HashMap<String, Schema> schemas;
    public SchemaRegistry() {
        loadSchemas();
    }

    public Set<String> getSchemaNames() {
        return schemas.keySet();
    }

    private void loadSchemas() {
        schemas = new HashMap<>();

        ResourceLoader loader = new ResourceLoader(CompactMachines3.class, ConfigurationHandler.schemaDirectory, "assets/compactmachines3/config/schemas/");
        for(Map.Entry<String, InputStream> entry : loader.getResources().entrySet()) {
            String filename = entry.getKey();
            InputStream is = entry.getValue();

            if (!filename.endsWith(".json")) {
                continue;
            }

            Schema schema = SerializationHelper.GSON.fromJson(new JsonReader(new InputStreamReader(is)), Schema.class);
            if(schema == null) {
                Logz.error("Could not deserialize schema from file: \"" + filename + "\"");
                continue;
            }

            Logz.info("Loaded schema: %s [size=%s, blocks=%d]", schema.getName(), schema.getSize(), schema.getBlocks().size());
            addSchema(schema);
        }
    }

    public void addSchema(Schema schema) {
        schemas.put(schema.getName(), schema);
    }

    public boolean hasSchema(String name) {
        return schemas.containsKey(name);
    }

    public Schema getSchema(String name) {
        return schemas.get(name);
    }
}
