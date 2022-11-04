package dev.compactmods.machines.room;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import static dev.compactmods.machines.room.graph.CompactRoomProvider.DATA_NAME;

public class ForgeCompactRoomProvider {
    @Nullable
    public static CompactRoomProvider instance() {
        try {
            final ServerLevel level = CompactDimension.forServer(ServerLifecycleHooks.getCurrentServer());
            return level.getDataStorage()
                    .computeIfAbsent(CompactRoomProvider::fromDisk, CompactRoomProvider::empty, DATA_NAME);
        } catch (MissingDimensionException e) {
            CompactRoomProvider.LOGS.fatal(e);
            return null;
        }
    }
}
