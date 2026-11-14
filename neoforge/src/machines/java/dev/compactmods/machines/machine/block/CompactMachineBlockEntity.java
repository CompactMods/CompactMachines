package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.IBoundCompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class CompactMachineBlockEntity extends BlockEntity implements IBoundCompactMachineBlockEntity {

    protected UUID owner;

    private MachineColor machineColor;

    @Nullable
    private Component customName;

    private final ItemStacksResourceHandler coreItemHandler;

    public CompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Machines.BlockEntities.MACHINE.get(), pos, state);
        this.machineColor = MachineColor.DEFAULT;

        final var machine = this;
        this.coreItemHandler = new ItemStacksResourceHandler(1) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                return resource.has(Rooms.DataComponents.BOUND_ROOM_CODE) ||
                        resource.has(Rooms.DataComponents.ROOM_TEMPLATE_ID);
            }

            @Override
            protected void onContentsChanged(int index, ItemStack previousContents) {
                super.onContentsChanged(index, previousContents);
                machine.markChangedAndSync();
            }
        };
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
        this.machineColor = components.get(Machines.DataComponents.MACHINE_COLOR);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
        builder.set(Machines.DataComponents.MACHINE_COLOR, this.machineColor);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput out) {
        super.removeComponentsFromTag(out);
        out.discard("CustomName");
        out.discard("machine_color");
    }

    @Override
    public void saveCustomOnly(ValueOutput output) {
        super.saveCustomOnly(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("core", coreItemHandler);
        this.machineColor = input.read("machine_color", MachineColor.CODEC).orElse(MachineColor.DEFAULT);
        this.customName = input.read("CustomName", ComponentSerialization.CODEC).orElse(null);
        this.owner = input.read(NBT_OWNER, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("core", coreItemHandler);
        output.store("machine_color", MachineColor.CODEC, getMachineColor());
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
        output.storeNullable(NBT_OWNER, UUIDUtil.CODEC, this.owner);
    }

    /**
     * Saves and pushes the BE's update tag to watching clients, so the core item
     * and machine color render changes without waiting for a chunk reload.
     */
    private void markChangedAndSync() {
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            final var state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    public Optional<String> connectedRoom() {
        final var core = this.coreItemHandler.getResource(0);
        if (core.isEmpty() || !core.has(Rooms.DataComponents.BOUND_ROOM_CODE))
            return Optional.empty();

        final var code = core.get(Rooms.DataComponents.BOUND_ROOM_CODE);
        return Optional.ofNullable(code);
    }

    public Optional<Component> getCustomName() {
        return Optional.ofNullable(customName);
    }

    @Override
    public MachineColor getMachineColor() {
        if (this.machineColor != null)
            return this.machineColor;

        return MachineColor.DEFAULT;
    }

    @Override
    public void setMachineColor(MachineColor machineColor) {
        if (machineColor != null) {
            this.machineColor = machineColor;
            this.markChangedAndSync();
        }
    }

    @Override
    public boolean setCore(@org.jspecify.annotations.Nullable Player player, ItemStack newCore) {
        if (newCore.has(Rooms.DataComponents.BOUND_ROOM_CODE) || newCore.has(Rooms.DataComponents.ROOM_TEMPLATE_ID)) {
            coreItemHandler.set(0, ItemResource.of(newCore), 1);
            newCore.shrink(1);
            this.markChangedAndSync();
            return true;
        }

        return false;
    }

    @Override
    public ItemStack popCore(@org.jspecify.annotations.Nullable Player player) {
        try (var tx = Transaction.openRoot()) {
            final var resource = coreItemHandler.getResource(0);
            if (!resource.isEmpty() && coreItemHandler.extract(resource, 1, tx) == 1) {
                tx.commit();
                this.markChangedAndSync();
                return resource.toStack(1);
            }
        }

        return ItemStack.EMPTY;
    }

    public ItemStacksResourceHandler coreHandler() {
        return coreItemHandler;
    }
}
