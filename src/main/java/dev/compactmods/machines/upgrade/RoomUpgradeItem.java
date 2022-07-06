package dev.compactmods.machines.upgrade;

import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.upgrade.RoomUpgradeHelper;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class RoomUpgradeItem extends Item {

    public RoomUpgradeItem(Properties props) {
        super(props);
    }

    @Override
    public Component getName(ItemStack stack) {
        String key = RoomUpgradeHelper.getTypeFrom(stack)
                .map(rl -> MachineRoomUpgrades.REGISTRY.get().getValue(rl))
                .map(def -> def.getTranslationKey(stack))
                .orElse(RoomUpgrade.UNNAMED_TRANS_KEY);

        return new TranslatableComponent(key);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> info, TooltipFlag flag) {
        // Show upgrade type while sneaking, or if advanced tooltips are on
        if(Screen.hasShiftDown() || flag.isAdvanced()) {
            RoomUpgradeHelper.getTypeFrom(stack).ifPresent(upgType -> {
                info.add(TranslationUtil.tooltip(Tooltips.ROOM_UPGRADE_TYPE, upgType));
            });
        }
    }
}
