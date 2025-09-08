package dev.compactmods.machines.machine.block;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.IBoundCompactMachineBlockEntity;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoundCompactMachineBlockEntity extends BlockEntity implements IBoundCompactMachineBlockEntity {

    protected UUID owner;
    private String roomCode;

    private MachineColor machineColor;

    @Nullable
    private Component customName;

    public BoundCompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.BlockEntities.MACHINE.get(), pos, state);
        this.machineColor = MachineColor.DEFAULT;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.roomCode = components.get(CMDataComponents.BOUND_ROOM_CODE);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
        this.machineColor = components.get(CMDataComponents.MACHINE_COLOR);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
        builder.set(CMDataComponents.BOUND_ROOM_CODE, this.roomCode);
        builder.set(CMDataComponents.MACHINE_COLOR, this.machineColor);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput out) {
        super.removeComponentsFromTag(out);
        out.discard("CustomName");
        out.discard(CMDataComponents.KEY_ROOM_CODE);
        out.discard(CMDataComponents.KEY_MACHINE_COLOR);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.machineColor = input.read(CMDataComponents.KEY_MACHINE_COLOR, MachineColor.CODEC).orElse(MachineColor.DEFAULT);
        this.customName = input.read("CustomName", ComponentSerialization.CODEC).orElse(null);
        this.roomCode = input.read(NBT_ROOM_CODE, Codec.STRING).orElse(null);
        this.owner = input.read(NBT_OWNER, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CMDataComponents.KEY_MACHINE_COLOR, MachineColor.CODEC, this.machineColor);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
        output.storeNullable(NBT_ROOM_CODE, Codec.STRING, this.roomCode);
        output.storeNullable(NBT_OWNER, UUIDUtil.CODEC, this.owner);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag data = super.getUpdateTag(provider);
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(out);
        return data.merge(out.buildResult());
    }

    public GlobalPos getLevelPosition() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    public void setConnectedRoom(String roomCode) {
        this.roomCode = roomCode;
        this.setChanged();
    }

    @NotNull
    public String connectedRoom() {
        return roomCode;
    }

    public Optional<Component> getCustomName() {
        return Optional.ofNullable(customName);
    }

    @Override
    public MachineColor getMachineColor() {
        return this.machineColor;
    }

    @Override
    public void setMachineColor(MachineColor machineColor) {
        this.machineColor = machineColor;
        this.setChanged();
    }
}
