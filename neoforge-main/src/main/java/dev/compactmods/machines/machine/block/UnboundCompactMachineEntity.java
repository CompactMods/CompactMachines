package dev.compactmods.machines.machine.block;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.block.IUnboundCompactMachineBlockEntity;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UnboundCompactMachineEntity extends BlockEntity implements IUnboundCompactMachineBlockEntity {

    private @Nullable ResourceLocation templateId;
    private @Nullable Component customName;

    public UnboundCompactMachineEntity(BlockPos pos, BlockState state) {
        super(Machines.BlockEntities.UNBOUND_MACHINE.get(), pos, state);
        this.templateId = null;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.templateId = components.get(CMDataComponents.ROOM_TEMPLATE_ID);
        this.customName = components.get(DataComponents.CUSTOM_NAME);

        final var desiredColor = components.get(CMDataComponents.MACHINE_COLOR);
        if (desiredColor != null) {
            this.setData(CMDataAttachments.MACHINE_COLOR, desiredColor);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(CMDataComponents.ROOM_TEMPLATE_ID, this.templateId);
        builder.set(CMDataComponents.MACHINE_COLOR, this.getData(CMDataAttachments.MACHINE_COLOR));
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(CMDataComponents.KEY_ROOM_TEMPLATE);
        tag.remove(CMDataComponents.KEY_MACHINE_COLOR);
        tag.remove("CustomName");
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.Provider holders) {
        super.loadAdditional(nbt, holders);
        if (nbt.contains(NBT_TEMPLATE_ID))
            this.templateId = ResourceLocation.parse(nbt.getString(NBT_TEMPLATE_ID));

        this.customName = nbt.read("CustomName", ComponentSerialization.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.Provider holders) {
        super.saveAdditional(nbt, holders);
        nbt.storeNullable(NBT_TEMPLATE_ID, ResourceLocation.CODEC, templateId);
        nbt.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider holders) {
        CompoundTag data = super.getUpdateTag(holders);
        saveAdditional(data, holders);
        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.templateId = tag.read(NBT_TEMPLATE_ID, ResourceLocation.CODEC).orElse(RoomTemplate.NO_TEMPLATE);
        this.customName = tag.read("CustomName", ComponentSerialization.CODEC).orElse(null);
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
}
