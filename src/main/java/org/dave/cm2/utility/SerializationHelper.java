package org.dave.cm2.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dave.cm2.miniaturization.MultiblockRecipe;
import org.dave.cm2.miniaturization.MultiblockRecipeSerializer;
import org.dave.cm2.schema.BlockInformation;
import org.dave.cm2.schema.BlockInformationSerializer;
import org.dave.cm2.schema.Schema;
import org.dave.cm2.schema.SchemaSerializer;

public class SerializationHelper {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(BlockInformation.class, new BlockInformationSerializer())
            .registerTypeAdapter(Schema.class, new SchemaSerializer())
            .registerTypeAdapter(MultiblockRecipe.class, new MultiblockRecipeSerializer())
            .create();

}
