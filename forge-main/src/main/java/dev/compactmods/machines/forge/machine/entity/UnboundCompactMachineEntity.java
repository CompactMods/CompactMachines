package dev.compactmods.machines.forge.machine.entity;

import dev.compactmods.machines.api.core.CMRegistryKeys;
import dev.compactmods.machines.api.machine.IMachineBlockEntity;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.forge.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UnboundCompactMachineEntity extends BlockEntity implements IMachineBlockEntity {

    private static final String NBT_TEMPLATE_ID = MachineNbt.NBT_TEMPLATE_ID;

    private ResourceLocation roomTemplateId;

    public UnboundCompactMachineEntity(BlockPos pos, BlockState state) {
        super(Machines.UNBOUND_MACHINE_ENTITY.get(), pos, state);
        this.roomTemplateId = RoomTemplate.NO_TEMPLATE;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBT_TEMPLATE_ID)) {
            roomTemplateId = new ResourceLocation(nbt.getString(NBT_TEMPLATE_ID));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        if (roomTemplateId != null)
            nbt.putString(NBT_TEMPLATE_ID, roomTemplateId.toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        if (!this.roomTemplateId.equals(RoomTemplate.NO_TEMPLATE))
            data.putString(NBT_TEMPLATE_ID, this.roomTemplateId.toString());

        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains(NBT_TEMPLATE_ID))
            roomTemplateId = new ResourceLocation(tag.getString(NBT_TEMPLATE_ID));
    }

    public ResourceKey<RoomTemplate> templateId() {
        return ResourceKey.create(CMRegistryKeys.ROOM_TEMPLATES, roomTemplateId);
    }

    public void setTemplate(ResourceLocation template) {
        this.roomTemplateId = template;
        this.setChanged();
    }

    public Optional<RoomTemplate> template() {
        if (level != null) {
            return level.registryAccess()
                    .registry(CMRegistryKeys.ROOM_TEMPLATES)
                    .map(reg -> reg.get(roomTemplateId));
        }

        return Optional.empty();
    }

    @Override
    public int getColor() {
        return this.template().map(RoomTemplate::color).orElse(0xFFFFFFFF);
    }
}
