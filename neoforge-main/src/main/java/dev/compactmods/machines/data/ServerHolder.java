package dev.compactmods.machines.data;

import net.minecraft.server.MinecraftServer;

public interface ServerHolder {
    MinecraftServer server();
    void setServer(MinecraftServer server);
}
