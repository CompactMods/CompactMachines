package dev.compactmods.machines.data.persistent;

import com.mojang.serialization.DataResult;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.graph.CompactMachineConnectionGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class MachineConnections extends SavedData {
    public static final String DATA_NAME = CompactMachines.MOD_ID + "_connections";

    public CompactMachineConnectionGraph graph;

    public MachineConnections() {
        graph = new CompactMachineConnectionGraph();
    }

    public static MachineConnections get(MinecraftServer server) {
        ServerLevel compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionDataStorage sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(MachineConnections::fromNbt, MachineConnections::new, DATA_NAME);
    }

    public static MachineConnections fromNbt(CompoundTag nbt) {
        MachineConnections c = new MachineConnections();
        if (nbt.contains("graph")) {
            CompoundTag graphNbt = nbt.getCompound("graph");
            DataResult<CompactMachineConnectionGraph> graphParseResult = CompactMachineConnectionGraph.CODEC.parse(NbtOps.INSTANCE, graphNbt);

            graphParseResult
                    .resultOrPartial(CompactMachines.LOGGER::error)
                    .ifPresent(g -> c.graph = g);
        }

        return c;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        if(graph != null) {
            DataResult<Tag> dataResult = CompactMachineConnectionGraph.CODEC.encodeStart(NbtOps.INSTANCE, graph);
            dataResult
                    .resultOrPartial(CompactMachines.LOGGER::error)
                    .ifPresent(gNbt -> {
                        nbt.put("graph", gNbt);
                    });
        }

        return nbt;
    }
}
