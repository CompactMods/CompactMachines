package dev.compactmods.machines.machine.graph.legacy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.location.LevelBlockPosition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class LegacyMachineLocationsGraph extends SavedData {

    private static final String DATA_KEY = "compactmachines_machines";

    private final Map<Integer, LevelBlockPosition> machineMapping;

    private LegacyMachineLocationsGraph() {
        this.machineMapping = new HashMap<>();
    }

    public static LegacyMachineLocationsGraph get(MinecraftServer server) throws MissingDimensionException {
        var compactDim = server.getLevel(Registration.COMPACT_DIMENSION);
        if(compactDim == null)
            throw new MissingDimensionException();

        return compactDim.getDataStorage().get(LegacyMachineLocationsGraph::load, DATA_KEY);
    }

    private static LegacyMachineLocationsGraph load(CompoundTag tag) {
        LegacyMachineLocationsGraph tmp = new LegacyMachineLocationsGraph();

        if(tag.contains("locations")) {
            final var locations = MachineData.CODEC.listOf()
                    .fieldOf("locations")
                    .codec()
                    .parse(NbtOps.INSTANCE, tag)
                    .getOrThrow(false, CompactMachines.LOGGER::error);

            locations.forEach(md -> tmp.machineMapping.putIfAbsent(md.machine, md.location));
        }

        return tmp;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public LevelBlockPosition getLocation(int legacyMachineId) {
        if(!machineMapping.containsKey(legacyMachineId))
            return null;

        return this.machineMapping.get(legacyMachineId);
    }

    private record MachineData(LevelBlockPosition location, int machine) {
        public static final Codec<MachineData> CODEC = RecordCodecBuilder.create(i -> i.group(
                LevelBlockPosition.CODEC.fieldOf("location").forGetter(MachineData::location),
                Codec.INT.fieldOf("machine").forGetter(MachineData::machine)
        ).apply(i, MachineData::new));
    }
}
