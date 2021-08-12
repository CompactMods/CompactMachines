package com.robotgryphon.compactmachines.data.player;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.serialization.DataResult;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.codec.CodecExtensions;
import com.robotgryphon.compactmachines.data.codec.NbtListCollector;
import com.robotgryphon.compactmachines.data.graph.IMachineGraphNode;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.util.PlayerUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

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
