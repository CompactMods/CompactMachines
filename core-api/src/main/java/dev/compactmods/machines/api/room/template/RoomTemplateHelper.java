package dev.compactmods.machines.api.room.template;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;

import java.util.Optional;
import java.util.stream.Stream;

public class RoomTemplateHelper {

	public static RoomTemplate getTemplate(LevelReader levelReader, ResourceLocation id) {
		return getTemplateOptional(levelReader.registryAccess(), id)
			.orElse(RoomTemplate.INVALID_TEMPLATE);
	}

	public static RoomTemplate getTemplate(RegistryAccess registryAccess, ResourceLocation id) {
		return getTemplateOptional(registryAccess, id)
			.orElse(RoomTemplate.INVALID_TEMPLATE);
	}

    public static Stream<RoomTemplate> getTemplates(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(RoomTemplate.REGISTRY_KEY).stream();
    }

    public static Stream<Holder.Reference<RoomTemplate>> getTemplateHolders(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(RoomTemplate.REGISTRY_KEY)
                .asLookup()
                .listElements();
    }

	public static Optional<RoomTemplate> getTemplateOptional(RegistryAccess registryAccess, ResourceLocation id) {
		return registryAccess.registryOrThrow(RoomTemplate.REGISTRY_KEY)
			.getOptional(id);
	}

	public static Holder.Reference<RoomTemplate> getTemplateHolder(LevelReader levelReader, ResourceLocation id) {
		return getTemplateHolder(levelReader.registryAccess(), id);
	}

	public static Holder.Reference<RoomTemplate> getTemplateHolder(RegistryAccess registryAccess, ResourceLocation id) {
		return registryAccess.registryOrThrow(RoomTemplate.REGISTRY_KEY)
			.getHolderOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));
	}
}
