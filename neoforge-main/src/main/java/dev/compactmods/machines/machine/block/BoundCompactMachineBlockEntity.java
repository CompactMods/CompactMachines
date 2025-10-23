package dev.compactmods.machines.machine.block;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.block.IBoundCompactMachineBlockEntity;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoundCompactMachineBlockEntity extends BlockEntity implements IBoundCompactMachineBlockEntity {

   protected UUID owner;
   private String roomCode;

   @Nullable
   private Component customName;

   public BoundCompactMachineBlockEntity(BlockPos pos, BlockState state) {
	  super(Machines.BlockEntities.MACHINE.get(), pos, state);
   }

	@Override
	protected void applyImplicitComponents(DataComponentInput components) {
		super.applyImplicitComponents(components);
		this.roomCode = components.get(CMDataComponents.BOUND_ROOM_CODE);
        this.customName = components.get(DataComponents.CUSTOM_NAME);

		final var desiredColor = components.get(CMDataComponents.MACHINE_COLOR);
		if (desiredColor != null) {
			this.setData(CMDataAttachments.MACHINE_COLOR, desiredColor);
		}
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.customName);
		builder.set(CMDataComponents.BOUND_ROOM_CODE, this.roomCode);
		builder.set(CMDataComponents.MACHINE_COLOR, this.getData(CMDataAttachments.MACHINE_COLOR));
	}

	@Override
	public void removeComponentsFromTag(CompoundTag tag) {
		super.removeComponentsFromTag(tag);
        tag.remove("CustomName");
		tag.remove(CMDataComponents.KEY_ROOM_CODE);
		tag.remove(CMDataComponents.KEY_MACHINE_COLOR);
	}

   @Override
   protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider holders) {
	  super.loadAdditional(nbt, holders);
      this.customName = nbt.read("CustomName", ComponentSerialization.CODEC).orElse(null);
      this.roomCode = nbt.read(NBT_ROOM_CODE, Codec.STRING).orElse(null);
      this.owner = nbt.read(NBT_OWNER, UUIDUtil.CODEC).orElse(null);
   }

   @Override
   protected void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.Provider holders) {
	  super.saveAdditional(nbt, holders);
      nbt.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
      nbt.storeNullable(NBT_ROOM_CODE, Codec.STRING, this.roomCode);
      nbt.storeNullable(NBT_OWNER, UUIDUtil.CODEC, this.owner);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
	  var data = super.getUpdateTag(provider);
	  saveAdditional(data, provider);
	  return data;
   }

    public void setOwner(UUID owner) {
	  this.owner = owner;
   }

    public GlobalPos getLevelPosition() {
	  return GlobalPos.of(level.dimension(), worldPosition);
   }

   public void setConnectedRoom(String roomCode) {
	  if (level != null && !level.isClientSide()) {
		 this.roomCode = roomCode;

		 CompactMachines.room(roomCode).ifPresentOrElse(inst -> {
				this.setData(CMDataAttachments.MACHINE_COLOR, inst.defaultMachineColor());
			 },
			 () -> {
				this.setData(CMDataAttachments.MACHINE_COLOR, MachineColors.WHITE);
			 });

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

}
