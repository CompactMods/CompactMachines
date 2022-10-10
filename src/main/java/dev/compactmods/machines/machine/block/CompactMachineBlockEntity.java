package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMRegistries;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.registration.IBasicRoomInfo;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.tunnels.connection.RoomTunnelConnections;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.machine.BasicRoomInfo;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.util.NbtUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class CompactMachineBlockEntity extends BlockEntity {

    /**
     * @deprecated Store a room code instead or use room lookup utils
     */
    @Deprecated(forRemoval = true, since = "5.2.0")
    public static final String NBT_ROOM_POS = "room_pos";
    public static final String NBT_ROOM_CODE = "room_code";
    public static final String NBT_CUSTOM_COLOR = "machine_color";
    public static final String NBT_TEMPLATE_ID = "template_id";
    private static final String NBT_ROOM_COLOR = "room_color";

    @Nullable
    private ResourceLocation roomTemplateId = null;

    protected UUID owner;
    private String roomCode;

    private boolean hasCustomColor = false;
    private int customColor;
    private int roomColor;

    private WeakReference<CompactMachineNode> graphNode;
    private WeakReference<IRoomRegistration> fullRoomInfo;

    @Nullable
    private IBasicRoomInfo basicInfo;

    public CompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.MACHINE_TILE_ENTITY.get(), pos, state);
        this.roomTemplateId = RoomTemplate.NO_TEMPLATE;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (level instanceof ServerLevel sl) {
            return getConnectedRoom().map(roomId -> {
                try {
                    final var serv = sl.getServer();
                    final var compactDim = CompactDimension.forServer(serv);

                    final var graph = TunnelConnectionGraph.forRoom(compactDim, roomId);

                    final var supportingTunnels = graph.getTunnelsSupporting(getLevelPosition(), side, cap);
                    final var firstSupported = supportingTunnels.findFirst();
                    if (firstSupported.isEmpty())
                        return super.getCapability(cap, side);

                    if (compactDim.getBlockEntity(firstSupported.get()) instanceof TunnelWallEntity tunnel) {
                        return tunnel.getTunnelCapability(cap, side);
                    } else {
                        return super.getCapability(cap, side);
                    }
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.fatal(e);
                    return super.getCapability(cap, side);
                }
            }).orElse(super.getCapability(cap, side));
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.syncConnectedRoom();
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBT_ROOM_POS)) {
            final var originalRoomPos = NbtUtil.readChunkPos(nbt.get(NBT_ROOM_POS));
            CompactMachines.LOGGER.debug("Machine block has a chunk position specified, it will be rewritten to the new room code system." +
                    "The block at {} was originally connected to position {}", worldPosition, originalRoomPos);
        }

        if (nbt.contains(NBT_ROOM_CODE)) {
            this.hasCustomColor = true;
            this.roomCode = nbt.getString(NBT_ROOM_CODE);
        }

        if (nbt.contains(MachineNbt.OWNER)) {
            owner = nbt.getUUID(MachineNbt.OWNER);
        } else {
            owner = null;
        }

        if (nbt.contains(NBT_CUSTOM_COLOR)) {
            customColor = nbt.getInt(NBT_CUSTOM_COLOR);
            hasCustomColor = true;
        }

        if (nbt.contains(NBT_TEMPLATE_ID)) {
            roomTemplateId = new ResourceLocation(nbt.getString(NBT_TEMPLATE_ID));
        }

        if(level != null && !level.isClientSide )
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        if (owner != null) {
            nbt.putUUID(MachineNbt.OWNER, this.owner);
        }

        if (hasCustomColor) {
            nbt.putInt(NBT_CUSTOM_COLOR, customColor);
        }

        if (roomCode != null)
            nbt.putString(NBT_ROOM_CODE, roomCode);

        if (roomTemplateId != null)
            nbt.putString(NBT_TEMPLATE_ID, roomTemplateId.toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        roomInfo().ifPresent(room -> {
            // data.putString(ROOM_POS_NBT, room);
            data.putString(NBT_ROOM_CODE, roomCode);
        });

        if (level instanceof ServerLevel) {
            // TODO - Internal player list
            if (this.owner != null)
                data.putUUID("owner", this.owner);
        }

        if (hasCustomColor)
            data.putInt(NBT_CUSTOM_COLOR, customColor);
        else
            data.putInt(NBT_ROOM_COLOR, getColor());

        if (!this.roomTemplateId.equals(RoomTemplate.NO_TEMPLATE))
            data.putString(NBT_TEMPLATE_ID, this.roomTemplateId.toString());

        return data;
    }

    private Optional<String> getConnectedRoom() {
        if (level instanceof ServerLevel sl) {
            if (roomCode != null)
                return Optional.of(roomCode);

            final var graph = DimensionMachineGraph.forDimension(sl);

            var chunk = graph.getConnectedRoom(worldPosition);
            chunk.ifPresent(c -> this.roomCode = c);
            return chunk;
        }

        return Optional.ofNullable(roomCode);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            // playerData = CompactMachinePlayerData.fromNBT(players);

        }

        if (tag.contains(NBT_ROOM_POS)) {
            int[] room = tag.getIntArray(NBT_ROOM_POS);
            final var oldChunk = new ChunkPos(room[0], room[1]);
        }

        if (tag.contains(NBT_CUSTOM_COLOR)) {
            hasCustomColor = true;
            customColor = tag.getInt(NBT_CUSTOM_COLOR);
        } else {
            roomColor = tag.getInt(NBT_ROOM_COLOR);
        }

        if (tag.contains("owner"))
            owner = tag.getUUID("owner");

        if (tag.contains(NBT_TEMPLATE_ID))
            roomTemplateId = new ResourceLocation(tag.getString(NBT_TEMPLATE_ID));

        this.basicInfo = new BasicRoomInfo(this.roomCode, this.getColor());
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean hasPlayersInside() {
        // TODO
        return false;
    }

    public LevelBlockPosition getLevelPosition() {
        return new LevelBlockPosition(level.dimension(), worldPosition);
    }

    public void syncConnectedRoom() {
        if (this.level == null || this.level.isClientSide) return;

        if (level instanceof ServerLevel sl) {
            final var graph = DimensionMachineGraph.forDimension(sl);
            try {
                final var compactDim = CompactDimension.forServer(sl.getServer());

                graph.getMachineNode(worldPosition).ifPresent(node -> {
                    this.graphNode = new WeakReference<>(node);
                });

                if (this.roomCode != null) {
                    final var lookup = CompactRoomProvider.instance(compactDim);
                    lookup.forRoom(roomCode).ifPresent(roomInfo -> {
                        this.basicInfo = new BasicRoomInfo(this.roomCode, this.getColor());
                        this.fullRoomInfo = new WeakReference<>(roomInfo);
                        this.roomColor = roomInfo.color();
                    });
                } else {
                    graph.getConnectedRoom(worldPosition).ifPresentOrElse(room -> {
                        CompactMachines.LOGGER.debug("No room code found for {}/{}, but machine connection graph has value {}; connecting to that room...",
                                level.dimension().location(), worldPosition.toShortString(), room);

                        this.roomCode = room;
                        final var lookup = CompactRoomProvider.instance(compactDim);
                        lookup.forRoom(roomCode).ifPresent(roomInfo -> {
                            this.basicInfo = new BasicRoomInfo(this.roomCode, this.getColor());
                            this.fullRoomInfo = new WeakReference<>(roomInfo);
                            this.roomColor = roomInfo.color();
                        });
                    }, () -> {
                        this.basicInfo = null;
                    });
                }

                this.setChanged();
            } catch (MissingDimensionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setConnectedRoom(IRoomRegistration room) {
        if (level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.connectMachineToRoom(worldPosition, room.code());
            this.roomCode = room.code();
            syncConnectedRoom();
        }
    }

    public void disconnect() {
        if (level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.disconnect(worldPosition);

            this.roomCode = null;
            this.graphNode.clear();
            setChanged();
        }
    }

    public Stream<BlockPos> getTunnels(Direction dir) {
        if (level == null || roomCode == null) return Stream.empty();

        if (level instanceof ServerLevel sl) {
            try {
                final ServerLevel compactDim = CompactDimension.forServer(sl.getServer());
                final var tunnelGraph = TunnelConnectionGraph.forRoom(compactDim, roomCode);
                return tunnelGraph.getTunnelsForSide(getLevelPosition(), dir).map(TunnelNode::position);
            } catch (MissingDimensionException e) {
                return Stream.empty();
            }
        }

        return Stream.empty();
    }

    public Optional<RoomTunnelConnections> getTunnelGraph() {
        if (level == null || roomCode == null) return Optional.empty();

        if (level instanceof ServerLevel sl) {
            try {
                final var compactDim = CompactDimension.forServer(sl.getServer());
                final var tunnelGraph = TunnelConnectionGraph.forRoom(compactDim, roomCode);
                return Optional.of(tunnelGraph);
            } catch (MissingDimensionException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public Optional<IRoomRegistration> roomInfo() {
        if (level != null && level.isClientSide) {
            if (roomCode == null) return Optional.empty();
            return Optional.empty();
        } else {
            if (fullRoomInfo == null)
                return Optional.empty();

            return Optional.ofNullable(fullRoomInfo.get());
        }
    }

    public int getColor() {
        return hasCustomColor ? customColor : roomColor;
    }

    public Optional<RoomTemplate> getRoomTemplate() {
        if (level != null) {
            return level.registryAccess()
                    .registry(CMRegistries.TEMPLATE_REG_KEY)
                    .map(reg -> reg.get(roomTemplateId));
        }

        return Optional.empty();
    }

    public Optional<String> connectedRoom() {
        return Optional.ofNullable(roomCode);
    }

    public Optional<IBasicRoomInfo> basicRoomInfo() {
        if (this.roomCode != null)
            return Optional.of(this.basicInfo);

        return Optional.empty();
    }
}
