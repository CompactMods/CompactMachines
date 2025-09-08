package dev.compactmods.machines.machine.block;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.IUnboundCompactMachineBlockEntity;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.machine.MachineColorSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UnboundCompactMachineEntity extends BlockEntity implements IUnboundCompactMachineBlockEntity {

    private MachineColor machineColor;
    private @Nullable ResourceLocation templateId;
    private @Nullable Component customName;

    public UnboundCompactMachineEntity(BlockPos pos, BlockState state) {
        super(Machines.BlockEntities.UNBOUND_MACHINE.get(), pos, state);
        this.templateId = null;
        this.machineColor = MachineColor.fromDyeColor(DyeColor.WHITE);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.templateId = components.get(CMDataComponents.ROOM_TEMPLATE_ID);
        this.customName = components.get(DataComponents.CUSTOM_NAME);
        this.machineColor = components.get(CMDataComponents.MACHINE_COLOR);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(CMDataComponents.ROOM_TEMPLATE_ID, this.templateId);
        builder.set(CMDataComponents.MACHINE_COLOR, this.machineColor);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard(CMDataComponents.KEY_ROOM_TEMPLATE);
        output.discard(CMDataComponents.KEY_MACHINE_COLOR);
        output.discard("CustomName");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.machineColor = input.read(CMDataComponents.KEY_MACHINE_COLOR, MachineColor.CODEC).orElse(MachineColor.DEFAULT);
        this.templateId = input.read(NBT_TEMPLATE_ID, ResourceLocation.CODEC).orElse(RoomTemplate.NO_TEMPLATE);
        this.customName = input.read("CustomName", ComponentSerialization.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CMDataComponents.KEY_MACHINE_COLOR, MachineColor.CODEC, this.machineColor);
        output.storeNullable(NBT_TEMPLATE_ID, ResourceLocation.CODEC, templateId);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider holders) {
        CompoundTag data = super.getUpdateTag(holders);
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, holders);
        saveAdditional(out);
        return data.merge(out.buildResult());
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        this.machineColor = input.read(CMDataComponents.KEY_MACHINE_COLOR, MachineColor.CODEC)
                .orElse(MachineColor.fromDyeColor(DyeColor.WHITE));

        this.templateId = input.read(NBT_TEMPLATE_ID, ResourceLocation.CODEC).orElse(RoomTemplate.NO_TEMPLATE);
        this.customName = input.read("CustomName", ComponentSerialization.CODEC).orElse(null);
    }

    public void setTemplate(ResourceLocation template) {
        this.templateId = template;
        this.setChanged();
    }

    @Nullable
    public ResourceLocation templateId() {
        return templateId;
    }

    @Nullable
    public Component customName() {
        return customName;
    }

    @Override
    public MachineColor getMachineColor() {
        return machineColor;
    }

    @Override
    public void setMachineColor(MachineColor newColor) {
        this.machineColor = newColor;
        this.setChanged();

        if(level != null && !level.isClientSide() && level instanceof ServerLevel sl) {
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(this.worldPosition),
                    new MachineColorSyncPacket(new GlobalPos(sl.dimension(), worldPosition), newColor));
        }
    }
}
