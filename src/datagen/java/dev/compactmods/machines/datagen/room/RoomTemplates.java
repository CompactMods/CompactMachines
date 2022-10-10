package dev.compactmods.machines.datagen.room;

import com.mojang.serialization.JsonOps;
import dev.compactmods.machines.api.core.CMRegistries;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.HashMap;

public class RoomTemplates {

    public static void make(GatherDataEvent event) {

        final var templates = new HashMap<ResourceLocation, RoomTemplate>();
        templates.put(new ResourceLocation(Constants.MOD_ID, "tiny"), LegacySizedTemplates.EMPTY_TINY.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "small"), LegacySizedTemplates.EMPTY_SMALL.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "normal"), LegacySizedTemplates.EMPTY_NORMAL.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "large"), LegacySizedTemplates.EMPTY_LARGE.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "giant"), LegacySizedTemplates.EMPTY_GIANT.template());
        templates.put(new ResourceLocation(Constants.MOD_ID, "colossal"), LegacySizedTemplates.EMPTY_COLOSSAL.template());

        templates.put(new ResourceLocation(Constants.MOD_ID, "weird"), new RoomTemplate(new Vec3i(42, 69, 12),
                FastColor.ARGB32.color(255, 0, 166, 88),
                RoomTemplate.NO_TEMPLATE));

        final var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.builtinCopy());

        final var gen = event.getGenerator();
        final var files = event.getExistingFileHelper();

        final var provider = JsonCodecProvider.forDatapackRegistry(gen, files,
                Constants.MOD_ID, ops, CMRegistries.TEMPLATE_REG_KEY, templates);

        gen.addProvider(event.includeServer(), provider);
    }
}
