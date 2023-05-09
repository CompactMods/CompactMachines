package dev.compactmods.machines.forge.machine.entity;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.machine.IMachineBlockEntity;
import dev.compactmods.machines.api.machine.MachineEntityNbt;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.tunnel.TunnelWallEntity;
import dev.compactmods.machines.forge.tunnel.graph.traversal.ForgeTunnelTypeFilters;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelMachineFilters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("removal")
public class BoundCompactMachineBlockEntity extends BlockEntity implements IMachineBlockEntity {

    protected UUID owner;
    private String roomCode;

    private boolean hasMachineColorOverride = false;
    private int machineColor;
    private int roomColor;

    @Nullable
    private Component customName;

    public BoundCompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.MACHINE_ENTITY.get(), pos, state);
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

                    final var firstSupported = graph.positions(
                            TunnelMachineFilters.sided(getLevelPosition(), side),
                            ForgeTunnelTypeFilters.capability(cap)
                    ).findFirst();

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
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(MachineEntityNbt.NBT_ROOM_CODE)) {
            this.roomCode = nbt.getString(MachineEntityNbt.NBT_ROOM_CODE);
        }

        if (nbt.contains(MachineNbt.OWNER)) {
            owner = nbt.getUUID(MachineNbt.OWNER);
        } else {
            owner = null;
        }

        if (nbt.contains(MachineEntityNbt.NBT_CUSTOM_COLOR)) {
            machineColor = nbt.getInt(MachineNbt.NBT_COLOR);
            hasMachineColorOverride = true;
        }

        if (level != null && !level.isClientSide)
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        if (owner != null) {
            nbt.putUUID(MachineNbt.OWNER, this.owner);
        }

        if (hasMachineColorOverride) {
            nbt.putInt(MachineEntityNbt.NBT_CUSTOM_COLOR, machineColor);
        }

        if (roomCode != null)
            nbt.putString(MachineEntityNbt.NBT_ROOM_CODE, roomCode);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        if (this.roomCode != null) {
            // data.putString(ROOM_POS_NBT, room);
            data.putString(MachineEntityNbt.NBT_ROOM_CODE, roomCode);
        }

        if (level instanceof ServerLevel) {
            // TODO - Internal player list
            if (this.owner != null)
                data.putUUID("owner", this.owner);
        }

        if (hasMachineColorOverride)
            data.putInt(MachineEntityNbt.NBT_CUSTOM_COLOR, machineColor);
        else
            data.putInt(MachineEntityNbt.NBT_ROOM_COLOR, getColor());

        return data;
    }

    private Optional<String> getConnectedRoom() {
        if (level instanceof ServerLevel sl) {
            if (roomCode != null)
                return Optional.of(roomCode);

            final var graph = DimensionMachineGraph.forDimension(sl);

            var chunk = graph.connectedRoom(worldPosition);
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

        if (tag.contains(MachineEntityNbt.NBT_ROOM_CODE)) {
            this.roomCode = tag.getString(MachineEntityNbt.NBT_ROOM_CODE);
        }

        if (tag.contains(MachineEntityNbt.NBT_CUSTOM_COLOR)) {
            hasMachineColorOverride = true;
            machineColor = tag.getInt(MachineNbt.NBT_COLOR);
        }

        if (tag.contains(MachineEntityNbt.NBT_ROOM_COLOR)) {
            roomColor = tag.getInt(MachineEntityNbt.NBT_ROOM_COLOR);
        }

        if (tag.contains("owner"))
            owner = tag.getUUID("owner");
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

    public GlobalPos getLevelPosition() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    public void setConnectedRoom(String roomCode) {
        if (level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            if (this.roomCode != null) {
                dimMachines.unregisterMachine(worldPosition);
            }

            dimMachines.register(worldPosition, roomCode);
            this.roomCode = roomCode;

            // FIXME - Rooms do not have colors on first creation; this should be pulled from the template!
            CompactRoomProvider.instance(sl.getServer()).forRoom(roomCode).ifPresent(roomInfo -> {
                this.roomColor = roomInfo.color();
            });

            this.setChanged();
        }
    }

    public void disconnect() {
        if (level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.unregisterMachine(worldPosition);

            this.roomCode = null;
            setChanged();
        }
    }

    public Optional<TunnelConnectionGraph> getTunnelGraph() {
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

    public int getColor() {
        return hasMachineColorOverride ? machineColor : roomColor;
    }

    public void setColor(int color) {
        this.machineColor = color;
        this.hasMachineColorOverride = true;
    }

    public Optional<String> connectedRoom() {
        return Optional.ofNullable(roomCode);
    }

    public Optional<Component> getCustomName() {
        return Optional.ofNullable(customName);
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
        this.setChanged();
    }
}
