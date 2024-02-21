package dev.compactmods.machines.neoforge.machine.block;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.machine.IColoredMachine;
import dev.compactmods.machines.neoforge.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoundCompactMachineBlockEntity extends BlockEntity implements IColoredMachine {

    protected UUID owner;
    private String roomCode;

    private boolean hasMachineColorOverride = false;
    private int machineColor;
    private int roomColor;

    @Nullable
    private Component customName;

    public static final String NBT_OWNER = "owner";
    public static final String NBT_COLOR = "machine_color";
    public static final String NBT_ROOM_CODE = "room_code";
    public static final String NBT_ROOM_COLOR = "room_color";

    public BoundCompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.MACHINE_ENTITY.get(), pos, state);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBT_ROOM_CODE)) {
            this.roomCode = nbt.getString(NBT_ROOM_CODE);
        }

        if (nbt.contains(NBT_OWNER)) {
            owner = nbt.getUUID(NBT_OWNER);
        } else {
            owner = null;
        }

        if(nbt.contains(NBT_ROOM_COLOR)) {
            roomColor = nbt.getInt(NBT_COLOR);
        }

        if (nbt.contains(NBT_COLOR)) {
            machineColor = nbt.getInt(NBT_COLOR);
            hasMachineColorOverride = true;
        } else {
            hasMachineColorOverride = false;
        }

        if (level != null && !level.isClientSide)
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);

        if (owner != null) {
            nbt.putUUID(NBT_OWNER, this.owner);
        }

        if (hasMachineColorOverride)
            nbt.putInt(NBT_COLOR, machineColor);

        nbt.putInt(NBT_ROOM_COLOR, roomColor);

        if (roomCode != null)
            nbt.putString(NBT_ROOM_CODE, roomCode);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        if (this.roomCode != null) {
            // data.putString(ROOM_POS_NBT, room);
            data.putString(NBT_ROOM_CODE, roomCode);
        }

        if (level instanceof ServerLevel) {
            // TODO - Internal player list
            if (this.owner != null)
                data.putUUID("owner", this.owner);
        }

        if (hasMachineColorOverride)
            data.putInt(NBT_COLOR, machineColor);
        else
            data.putInt(NBT_ROOM_COLOR, getColor());

        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            // playerData = CompactMachinePlayerData.fromNBT(players);

        }

        if (tag.contains(NBT_ROOM_CODE)) {
            this.roomCode = tag.getString(NBT_ROOM_CODE);
        }

        if (tag.contains(NBT_ROOM_COLOR)) {
            roomColor = tag.getInt(NBT_ROOM_COLOR);
        }

        if (tag.contains(NBT_COLOR)) {
            hasMachineColorOverride = true;
            machineColor = tag.getInt(NBT_COLOR);
        } else {
            hasMachineColorOverride = false;
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
            // FIXME: Register machine location in room's connection graph
//            final var dimMachines = DimensionMachineGraph.forDimension(sl);
//            if (this.roomCode != null) {
//                dimMachines.unregisterMachine(worldPosition);
//            }
//
//            dimMachines.register(worldPosition, roomCode);
            this.roomCode = roomCode;

            RoomApi.room(roomCode).ifPresentOrElse(inst -> {
                this.roomColor = inst.defaultMachineColor();
                this.hasMachineColorOverride = false;
            }, () -> {
                this.roomColor = DyeColor.WHITE.getTextColor();
            });

            this.setChanged();
        }
    }

    public void disconnect() {
        if (level instanceof ServerLevel sl) {
            // FIXME: Room machine graph unregister
//            final var dimMachines = DimensionMachineGraph.forDimension(sl);
//            dimMachines.unregisterMachine(worldPosition);

            sl.setBlock(worldPosition, Machines.UNBOUND_MACHINE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public int getColor() {
        return hasMachineColorOverride ? machineColor : roomColor;
    }

    public void setColor(int color) {
        if(color != roomColor) {
            this.machineColor = color;
            this.hasMachineColorOverride = true;
            this.setChanged();
        }
    }

    @NotNull
    public String connectedRoom() {
        return roomCode;
    }

    public Optional<Component> getCustomName() {
        return Optional.ofNullable(customName);
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
        this.setChanged();
    }
}
