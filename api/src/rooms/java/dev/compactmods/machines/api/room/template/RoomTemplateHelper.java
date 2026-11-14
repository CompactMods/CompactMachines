package dev.compactmods.machines.api.room.template;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelReader;

import java.util.Optional;
import java.util.stream.Stream;

public class RoomTemplateHelper {

	public static RoomTemplate getTemplate(LevelReader levelReader, Identifier id) {
		return getTemplateOptional(levelReader.registryAccess(), id)
			.orElse(RoomTemplate.INVALID_TEMPLATE);
	}

	public static RoomTemplate getTemplate(RegistryAccess registryAccess, Identifier id) {
		return getTemplateOptional(registryAccess, id)
			.orElse(RoomTemplate.INVALID_TEMPLATE);
	}

    public static Stream<RoomTemplate> getTemplates(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(RoomTemplate.REGISTRY_KEY).stream();
    }

    public static Stream<Holder.Reference<RoomTemplate>> getTemplateHolders(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                .listElements();
    }

	public static Optional<RoomTemplate> getTemplateOptional(RegistryAccess registryAccess, Identifier id) {
		return registryAccess.lookupOrThrow(RoomTemplate.REGISTRY_KEY)
			.getOptional(id);
	}

	public static Holder.Reference<RoomTemplate> getTemplateHolder(LevelReader levelReader, Identifier id) {
		return getTemplateHolder(levelReader.registryAccess(), id);
	}

	public static Holder.Reference<RoomTemplate> getTemplateHolder(RegistryAccess registryAccess, Identifier id) {
		return registryAccess.lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                .getOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));
	}
}
