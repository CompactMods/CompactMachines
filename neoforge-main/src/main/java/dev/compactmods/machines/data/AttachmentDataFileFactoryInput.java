package dev.compactmods.machines.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

public record AttachmentDataFileFactoryInput<T>(MinecraftServer server, CompoundTag attachmentData, T additionalData) {
}
