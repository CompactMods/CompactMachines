package dev.compactmods.machines.item;

import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.client.gui.PersonalShrinkingDeviceScreen;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPersonalShrinkingDevice extends Item {

    public ItemPersonalShrinkingDevice(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (Screen.hasShiftDown()) {
            tooltip.add(TranslationUtil.tooltip(Tooltips.Details.PERSONAL_SHRINKING_DEVICE)
                    .withStyle(TextFormatting.YELLOW));
        } else {
            tooltip.add(TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                    .withStyle(TextFormatting.DARK_GRAY)
                    .withStyle(TextFormatting.ITALIC));
        }

    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == Hand.OFF_HAND) {
            return ActionResult.fail(stack);
        }

        // If we aren't in the compact dimension, allow PSD guide usage
        // Prevents misfiring if a player is trying to leave a machine or set their spawn
        if (world.isClientSide && world.dimension() != Registration.COMPACT_DIMENSION) {
            PersonalShrinkingDeviceScreen.show();
            return ActionResult.success(stack);
        }

        if (world instanceof ServerWorld && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            if (serverPlayer.level.dimension() == Registration.COMPACT_DIMENSION) {
                ServerWorld serverWorld = serverPlayer.getLevel();
                if (player.isShiftKeyDown()) {
                    ChunkPos machineChunk = new ChunkPos(player.blockPosition());

                    CompactRoomData intern = CompactRoomData.get(serverWorld.getServer());
                    if (intern != null) {
                        // Use internal data to set new spawn point
                        intern.setSpawn(machineChunk, player.position());

                        IFormattableTextComponent tc = TranslationUtil.message(Messages.MACHINE_SPAWNPOINT_SET)
                                .withStyle(TextFormatting.GREEN);

                        player.displayClientMessage(tc, true);
                    }
                } else {
                    PlayerUtil.teleportPlayerOutOfMachine(serverWorld, serverPlayer);
                }
            }
        }

        return ActionResult.success(stack);
    }
}
