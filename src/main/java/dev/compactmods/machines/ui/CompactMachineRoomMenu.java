package dev.compactmods.machines.ui;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.UIRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompactMachineRoomMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos pos;
    private final Player player;

    public CompactMachineRoomMenu(int win, Level level, BlockPos pos, Player player) {
        super(UIRegistration.MACHINE_MENU.get(), win);
        this.level = level;
        this.pos = pos;
        this.player = player;
    }

    public static MenuProvider makeProvider(BlockPos pos, Player player) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(CompactMachines.MOD_ID + ".ui.room");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int p_39954_, Inventory inv, Player player2) {
                return new CompactMachineRoomMenu(p_39954_, player.level, pos, player);
            }
        };
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slotInd) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
