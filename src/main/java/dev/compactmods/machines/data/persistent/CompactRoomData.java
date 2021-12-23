package dev.compactmods.machines.data.persistent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.OperationNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.codec.CodecExtensions;
import dev.compactmods.machines.data.codec.NbtListCollector;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CompactRoomData extends SavedData {
    public static final String DATA_NAME = CompactMachines.MOD_ID + "_rooms";

    private final Map<ChunkPos, RoomData> machineData;

    public CompactRoomData() {
        machineData = new HashMap<>();
    }

    @Nullable
    public static CompactRoomData get(MinecraftServer server) {
        ServerLevel compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionDataStorage sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(CompactRoomData::fromNbt, CompactRoomData::new, DATA_NAME);
    }

    public static CompactRoomData fromNbt(CompoundTag nbt) {
        CompactRoomData data = new CompactRoomData();
        if (nbt.contains("machines")) {
            ListTag machines = nbt.getList("machines", Tag.TAG_COMPOUND);
            machines.forEach(machNbt -> {
                DataResult<RoomData> result =
                        RoomData.CODEC.parse(NbtOps.INSTANCE, machNbt);

                result
                        .resultOrPartial((err) -> CompactMachines.LOGGER.error("Error loading machine data from file: {}", err))
                        .ifPresent(imd -> {
                            ChunkPos chunk = new ChunkPos(imd.getCenter());
                            data.machineData.put(chunk, imd);
                        });
            });
        }

        return data;
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        if (!machineData.isEmpty()) {
            ListTag collect = machineData.values()
                    .stream()
                    .map(data -> {
                        DataResult<Tag> n = RoomData.CODEC.encodeStart(NbtOps.INSTANCE, data);
                        return n.result();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(NbtListCollector.toNbtList());

            nbt.put("machines", collect);
        }

        return nbt;
    }

    public boolean isRegistered(ChunkPos chunkPos) {
        return machineData.containsKey(chunkPos);
    }

    private void register(ChunkPos pos, RoomData data) throws OperationNotSupportedException {
        if (isRegistered(pos))
            throw new OperationNotSupportedException("Machine already registered.");

        machineData.put(pos, data);
        setDirty();
    }

    @Nullable
    public DimensionalPosition getSpawn(ChunkPos roomChunk) {
        RoomData roomData = machineData.get(roomChunk);
        if (roomData == null)
            return null;

        return new DimensionalPosition(
                Registration.COMPACT_DIMENSION,
                roomData.getSpawn()
        );
    }

    public int getNextId() {
        return this.machineData.size() + 1;
    }

    public void setSpawn(ChunkPos roomChunk, Vec3 position) {
        if (!machineData.containsKey(roomChunk))
            return;

        RoomData roomData = machineData.get(roomChunk);
        roomData.setSpawn(position);

        setDirty();
    }

    public Optional<AABB> getInnerBounds(ChunkPos roomChunk) {
        if (!machineData.containsKey(roomChunk))
            return Optional.empty();

        AABB bounds = machineData.get(roomChunk).getMachineBounds();
        return Optional.of(bounds);
    }

    public NewRoomRegistration createNew() {
        return new NewRoomRegistration(this);
    }

    public boolean isMachineRoomChunk(ChunkPos pos) {
        return machineData.containsKey(pos);
    }

    public static class NewRoomRegistration {

        private final CompactRoomData storage;
        private Vec3 spawn;
        private ChunkPos chunk = new ChunkPos(0, 0);
        private EnumMachineSize size = EnumMachineSize.TINY;
        private BlockPos center = BlockPos.ZERO;
        private UUID owner;

        public NewRoomRegistration(CompactRoomData storage) {
            this.storage = storage;
        }

        private void recalculateSize() {
            BlockPos centerAtFloor = MathUtil.getCenterWithY(chunk, ServerConfig.MACHINE_FLOOR_Y.get());
            BlockPos centerSized = centerAtFloor.above(size.getInternalSize() / 2);

            this.spawn = new Vec3(centerAtFloor.getX(), centerAtFloor.getY(), centerAtFloor.getZ());
            this.center = centerSized;
        }

        public NewRoomRegistration owner(UUID owner) {
            this.owner = owner;
            return this;
        }

        public NewRoomRegistration size(EnumMachineSize size) {
            this.size = size;
            recalculateSize();
            return this;
        }

        public NewRoomRegistration spawn(BlockPos spawn) {
            Vec3 spawnTest = new Vec3(spawn.getX(), spawn.getY(), spawn.getZ());

            // Make sure the spawn is inside the new room bounds
            if(size.getBounds(this.center).contains(spawnTest))
                this.spawn = spawnTest;

            return this;
        }

        public NewRoomRegistration chunk(ChunkPos chunk) {
            this.chunk = chunk;
            recalculateSize();
            return this;
        }

        public void register() throws OperationNotSupportedException {
            RoomData data = new RoomData(owner, center, spawn, size);
            storage.register(chunk, data);
        }
    }

    private static class RoomData {

        public static final Codec<RoomData> CODEC = RecordCodecBuilder.create(i -> i.group(
                CodecExtensions.UUID_CODEC.fieldOf("owner").forGetter(RoomData::getOwner),
                BlockPos.CODEC.fieldOf("center").forGetter(RoomData::getCenter),
                CodecExtensions.VECTOR3D.fieldOf("spawn").forGetter(RoomData::getSpawn),
                EnumMachineSize.CODEC.fieldOf("size").forGetter(RoomData::getSize)
        ).apply(i, RoomData::new));

        private final UUID owner;
        private final BlockPos center;
        private Vec3 spawn;
        private final EnumMachineSize size;

        public RoomData(UUID owner, BlockPos center, Vec3 spawn, EnumMachineSize size) {
            this.owner = owner;
            this.center = center;
            this.spawn = spawn;
            this.size = size;
        }

        private EnumMachineSize getSize() {
            return this.size;
        }

        public UUID getOwner() {
            return this.owner;
        }

        public Vec3 getSpawn() {
            if (this.spawn != null)
                return this.spawn;

            Vec3 newSpawn = new Vec3(
                    center.getX(),
                    center.getY(),
                    center.getZ()
            );

            double offset = size.getInternalSize() / 2.0d;

            this.spawn = newSpawn.subtract(0, offset, 0);
            return this.spawn;
        }

        public BlockPos getCenter() {
            return this.center;
        }

        public void setSpawn(Vec3 newSpawn) {
            this.spawn = newSpawn;
        }

        public AABB getMachineBounds() {
            return size.getBounds(this.center);
        }
    }
}
