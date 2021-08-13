package dev.compactmods.machines.data.player;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Holds basic information about players inside a compact machine.
 */
public class CompactMachinePlayerData extends WorldSavedData {

    public static final String DATA_NAME = "players";

    public MutableGraph<IPlayerHistoryNode> history;
    private HashMap<UUID, IPlayerHistoryNode> lookup;

    protected CompactMachinePlayerData() {
        super(DATA_NAME);
        this.history = GraphBuilder
                .directed()
                .build();

        this.lookup = new HashMap<>(0);
    }

    @Nullable
    public static CompactMachinePlayerData get(MinecraftServer server) {
        ServerWorld compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionSavedDataManager sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(CompactMachinePlayerData::new, DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {

    }

    @Override
    @Nonnull
    public CompoundNBT save(CompoundNBT nbt) {
        return nbt;
    }
}
