package dev.compactmods.machines.datagen.room;

import com.mojang.serialization.JsonOps;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomTemplate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.HashMap;

public class RoomTemplates {

    public static void make(GatherDataEvent event) {
        final var tiny = new RoomTemplate(3, FastColor.ARGB32.color(255, 201, 91, 19));
        final var small = new RoomTemplate(5, FastColor.ARGB32.color(255, 212, 210, 210));
        final var normal = new RoomTemplate(7, FastColor.ARGB32.color(255, 251, 242, 54));
        final var large = new RoomTemplate(9, FastColor.ARGB32.color(255, 33, 27, 46));
        final var giant = new RoomTemplate(11, FastColor.ARGB32.color(255, 67, 214, 205));
        final var max = new RoomTemplate(13, FastColor.ARGB32.color(255, 66, 63, 66));

        final var templates = new HashMap<ResourceLocation, RoomTemplate>();
        templates.put(new ResourceLocation(Constants.MOD_ID, "tiny"), tiny);
        templates.put(new ResourceLocation(Constants.MOD_ID, "small"), small);
        templates.put(new ResourceLocation(Constants.MOD_ID, "normal"), normal);
        templates.put(new ResourceLocation(Constants.MOD_ID, "large"), large);
        templates.put(new ResourceLocation(Constants.MOD_ID, "giant"), giant);
        templates.put(new ResourceLocation(Constants.MOD_ID, "maximum"), max);

        final var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

        final var gen = event.getGenerator();
        final var files = event.getExistingFileHelper();

        final var provider = JsonCodecProvider.forDatapackRegistry(gen, files,
                Constants.MOD_ID, ops, dev.compactmods.machines.api.room.Rooms.TEMPLATE_REG_KEY, templates);

        gen.addProvider(event.includeServer(), provider);
    }
}
