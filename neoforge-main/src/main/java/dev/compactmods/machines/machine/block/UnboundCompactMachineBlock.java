package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnboundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
	public UnboundCompactMachineBlock(Properties props) {
		super(props);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		if (level.getBlockEntity(pos) instanceof UnboundCompactMachineEntity be) {
			final var id = be.templateId();
			if (id != null) {
				final var template = RoomTemplateHelper.getTemplateHolder(level, id);
				var item = Machines.Items.forNewRoom(template);
				be.getExistingData(CMDataAttachments.MACHINE_COLOR).ifPresent(color -> {
					item.set(CMDataComponents.MACHINE_COLOR, color);
				});

                final var cn = be.customName();
                if(cn != null)
                    item.set(DataComponents.CUSTOM_NAME, cn);

				return item;
			}
		}

		return Machines.Items.unbound();
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new UnboundCompactMachineEntity(pos, state);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_316595_, BlockHitResult p_316140_) {
		if (stack.getItem() instanceof DyeItem dye && !level.isClientSide && level instanceof ServerLevel serverLevel) {
			return tryDyingMachine(serverLevel, pos, player, dye, stack);
		}

		MinecraftServer server = level.getServer();
		if ((stack.is(PSDTags.ITEM) || stack.has(Shrinking.DataComponents.SHRINKING_CONFIG)) && player instanceof ServerPlayer sp) {
			level.getBlockEntity(pos, Machines.BlockEntities.UNBOUND_MACHINE.get()).ifPresent(unboundEntity -> {

				RoomTemplate template = RoomTemplateHelper.getTemplate(level, unboundEntity.templateId());
				if (!template.equals(RoomTemplate.INVALID_TEMPLATE)) {
					var color = unboundEntity.getData(CMDataAttachments.MACHINE_COLOR);

					try {
						// Generate a new machine room
						final var newRoom = CompactMachines.newRoom(server, template, sp.getUUID());
						newRoom.setData(CMDataAttachments.ROOM_OWNER, player.getUUID());

						// Change into a bound machine block
						level.setBlock(pos, Machines.Blocks.BOUND_MACHINE.get().defaultBlockState(), Block.UPDATE_ALL);

						// Set up binding and enter
						level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get()).ifPresent(ent -> {
							ent.setConnectedRoom(newRoom.code());
							ent.setData(CMDataAttachments.MACHINE_COLOR, color);

							try {
								RoomHelper.teleportPlayerIntoRoom(server, sp, newRoom, RoomEntryPoint.playerEnteringMachine(player))
										.thenAccept(res -> PersonalShrinkingDevice.handleSuccessfulAtomicShift(stack, sp, PersonalShrinkingDevice.config(stack)));
							} catch (MissingDimensionException e) {
								throw new RuntimeException(e);
							}
						});

					} catch (MissingDimensionException e) {
						LoggingUtil.modLog().error("Error occurred while generating new room and machine info for first player entry.", e);
					}
				} else {
					LoggingUtil.modLog().fatal("Tried to create and enter an invalidly-registered room. Something went very wrong!");
				}
			});
		}

		return super.useItemOn(stack, state, level, pos, player, p_316595_, p_316140_);
	}
}
