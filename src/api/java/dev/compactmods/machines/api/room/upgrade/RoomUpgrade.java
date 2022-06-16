package dev.compactmods.machines.api.room.upgrade;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface RoomUpgrade extends IForgeRegistryEntry<RoomUpgrade> {

    ResourceKey<Registry<RoomUpgrade>> REG_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "room_upgrade"));

    /**
     * Called when an upgrade is first applied to a room.
     */
    default void onAdded(ServerLevel level, ChunkPos room) {}

    /**
     * Called when an update is removed from a room.
     */
    default void onRemoved(ServerLevel level, ChunkPos room) {}
}
