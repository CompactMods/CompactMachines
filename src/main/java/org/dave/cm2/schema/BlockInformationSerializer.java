package org.dave.cm2.schema;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import org.dave.cm2.utility.Logz;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

public class BlockInformationSerializer implements JsonSerializer<BlockInformation>, JsonDeserializer<BlockInformation> {

    @Override
    public JsonElement serialize(BlockInformation src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("block", src.block.getRegistryName().toString());
        root.addProperty("x", src.position.getX());
        root.addProperty("y", src.position.getY());
        root.addProperty("z", src.position.getZ());
        if(src.meta != 0) {
            root.addProperty("meta", src.meta);
        }

        if(src.nbt != null) {
            // The problem when encoding this ourselves: we need to track the types of each entry,
            // so we can actually deserialize it later. Our code does not do this yet, so a readable
            // nbt schema is not possible atm.
            //root.add("nbt-own", NbtToJson(src.nbt));

            // The minecraft native logic to turn this into "some sort of json" is not fully implemented
            // and does not support NBTByteArrays (sigh). They are not properly encoded and as such this
            // method can not be used either when we want to reliably decode it again later.
            //root.addProperty("nbt", src.nbt.toString());

            // Instead we have to store the tag natively compressed by minecraft and encoded with base64
            // so we don't screw up our file encoding or have 00s anywhere in our json file.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream base64os = Base64.getEncoder().wrap(baos);
            try {
                CompressedStreamTools.writeCompressed(src.nbt, base64os);
            } catch (IOException e) {
            }

            if(baos != null) {
                root.addProperty("nbt", baos.toString());
            }

            // In case a tile entity does not write its x, y and z position to its nbt data
            // we won't overwrite it when recreating the tile entity.
            if(src.writePositionData == false) {
                root.addProperty("skipPositionData", true);
            }
        }

        return root;
    }

    private static JsonElement NbtToJson(NBTTagCompound tag) {
        Set<String> keys = tag.getKeySet();
        JsonObject jsonRoot = new JsonObject();
        for(String key : keys) {
            NBTBase nbt = tag.getTag(key);

            JsonElement element;
            if(nbt instanceof NBTTagCompound) {
                element = NbtToJson((NBTTagCompound) nbt);
            } else if(nbt instanceof NBTPrimitive) {
                String NBTType = NBTBase.NBT_TYPES[nbt.getId()];
                if(NBTType.equals("BYTE")) {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getByte());
                } else if(NBTType.equals("SHORT")) {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getShort());
                } else if(NBTType.equals("INT")) {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getInt());
                } else if(NBTType.equals("LONG")) {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getLong());
                } else if(NBTType.equals("FLOAT")) {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getFloat());
                } else if(NBTType.equals("DOUBLE")) {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getDouble());
                } else {
                    element = new JsonPrimitive(((NBTPrimitive) nbt).getDouble());
                }
            } else if(nbt instanceof NBTTagString) {
                element = new JsonPrimitive(((NBTTagString)nbt).getString());
            } else if(nbt instanceof NBTTagList) {
                NBTTagList tagList = (NBTTagList)nbt;
                JsonArray array = new JsonArray();
                for(int i = 0; i < tagList.tagCount(); i++) {
                    array.add(NbtToJson(tagList.getCompoundTagAt(i)));
                }
                element = array;
            } else if(nbt instanceof NBTTagIntArray) {
                int[] intArray = ((NBTTagIntArray) nbt).getIntArray();
                JsonArray array = new JsonArray();
                for (int value : intArray) {
                    array.add(new JsonPrimitive(value));
                }
                element = array;
            } else if(nbt instanceof NBTTagByteArray) {
                byte[] byteArray = ((NBTTagByteArray) nbt).getByteArray();
                JsonArray array = new JsonArray();
                for (byte value : byteArray) {
                    array.add(new JsonPrimitive(value));
                }
                element = array;
            } else {
                Logz.info(nbt + " no support for: " + NBTBase.NBT_TYPES[nbt.getId()]);
                throw new IllegalArgumentException("NBTtoJSON doesn't support nbt base type=" + NBTBase.NBT_TYPES[nbt.getId()] + ", tag=" + nbt);
            }

            jsonRoot.add(key, element);
        }
        return jsonRoot;
    }


    @Override
    public BlockInformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return null;
        }

        JsonObject jsonRoot = json.getAsJsonObject();
        BlockPos position = new BlockPos(jsonRoot.get("x").getAsInt(), jsonRoot.get("y").getAsInt(), jsonRoot.get("z").getAsInt());
        Block block = Block.getBlockFromName(jsonRoot.get("block").getAsString());
        int meta = 0;
        if(jsonRoot.has("meta")) {
            meta = jsonRoot.get("meta").getAsInt();
        }

        NBTTagCompound nbt = null;
        if(jsonRoot.has("nbt")) {
            InputStream is = new ByteArrayInputStream(jsonRoot.get("nbt").getAsString().getBytes(StandardCharsets.UTF_8));
            InputStream wrappedIs = Base64.getDecoder().wrap(is);
            try {
                nbt = CompressedStreamTools.readCompressed(wrappedIs);
            } catch (IOException e) {
            }
        }

        boolean writePositionData = !jsonRoot.has("skipPositionData");
        return new BlockInformation(position, block, meta, nbt, writePositionData);
    }
}
