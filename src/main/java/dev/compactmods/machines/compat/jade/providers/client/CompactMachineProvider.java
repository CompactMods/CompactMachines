package dev.compactmods.machines.compat.jade.providers.client;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public class CompactMachineProvider implements IBlockComponentProvider {
    public static final CompactMachineProvider INSTANCE = new CompactMachineProvider();

    @Override
    public void appendTooltip(
            ITooltip tooltip,
            BlockAccessor accessor,
            IPluginConfig config
    ) {
        final CompactMachineBlockEntity machine = (CompactMachineBlockEntity) accessor.getBlockEntity();
        machine.getConnectedRoom().ifPresentOrElse(room -> {
            tooltip.add(TranslationUtil.tooltip(Tooltips.Machines.BOUND_TO, room));
        }, () -> {
            MutableComponent newMachine = TranslationUtil
                    .message(new ResourceLocation(Constants.MOD_ID, "new_machine"))
                    .withStyle(ChatFormatting.GREEN);
            tooltip.add(newMachine);
        });

        machine.getOwnerUUID().ifPresent(ownerID -> {
            // Owner Name
            Player owner = accessor.getLevel().getPlayerByUUID(ownerID);
            if (owner != null) {
                MutableComponent ownerName = TranslationUtil
                        .tooltip(Tooltips.Machines.OWNER, owner.getDisplayName())
                        .withStyle(ChatFormatting.GRAY);
                tooltip.add(ownerName);
            }
        });

        if (accessor.getServerData().contains("attached_tunnels")) {
            ListTag tag = (ListTag) accessor.getServerData().get("attached_tunnels");
            tooltip.add(Component.literal("")); // New Line
            tag.forEach(t -> {
                CompoundTag compound = (CompoundTag) t;
                ItemStack itemStack = ItemStack.of(compound);
                IElementHelper helper = tooltip.getElementHelper();
                IElement icon = helper
                        .item(itemStack)
                        .size(new Vec2(15, 15))
                        .translate(new Vec2(-4, -3));
                tooltip.append(icon);
            });
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Constants.MOD_ID, "machine");
    }
}
