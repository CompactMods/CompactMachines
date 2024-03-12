package dev.compactmods.machines.neoforge.machine.block;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.machine.IColoredMachine;
import dev.compactmods.machines.neoforge.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UnboundCompactMachineEntity extends BlockEntity implements IColoredMachine {

    public static final String NBT_TEMPLATE_ID = "template_id";

    private RoomTemplate template;
    private @Nullable ResourceLocation templateId;

    public UnboundCompactMachineEntity(BlockPos pos, BlockState state) {
        super(Machines.UNBOUND_MACHINE_ENTITY.get(), pos, state);
        this.template = RoomTemplate.INVALID_TEMPLATE;
        this.templateId = null;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBT_TEMPLATE_ID)) {
            templateId = new ResourceLocation(nbt.getString(NBT_TEMPLATE_ID));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        loadTemplateFromID();
    }

    private void loadTemplateFromID() {
        if(templateId != null && level != null && !level.isClientSide) {
            final var server = level.getServer();
            this.template = RoomApi.getTemplates(server).get(templateId);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        if (templateId != null)
            nbt.putString(NBT_TEMPLATE_ID, templateId.toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        if (!this.template.equals(RoomTemplate.INVALID_TEMPLATE))
            data.putString(NBT_TEMPLATE_ID, this.templateId.toString());

        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains(NBT_TEMPLATE_ID))
            templateId = new ResourceLocation(tag.getString(NBT_TEMPLATE_ID));
    }

    public void setTemplate(ResourceLocation template) {
        this.templateId = template;
        this.loadTemplateFromID();
        this.setChanged();
    }

    public Optional<RoomTemplate> template() {
        return Optional.ofNullable(template);
    }

    @Override
    public int getColor() {
        return this.template().map(RoomTemplate::color).orElse(0xFFFFFFFF);
    }

    @Nullable
    public ResourceLocation templateId() {
        return templateId;
    }
}
