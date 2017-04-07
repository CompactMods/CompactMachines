package org.dave.cm2.schema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerializationHelper {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(BlockInformation.class, new BlockInformationSerializer())
            .registerTypeAdapter(Schema.class, new SchemaSerializer())
            .create();

}
