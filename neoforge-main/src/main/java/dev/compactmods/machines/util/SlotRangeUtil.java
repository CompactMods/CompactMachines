package dev.compactmods.machines.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.inventory.SlotRange;

public class SlotRangeUtil {

    public static final SlotRange HOTBAR = makeSlotRange( "hotbar", 0, 9);
    public static final SlotRange MAIN_PLAYER_INV = makeSlotRange("main_inventory", 9, 27);
    public static final SlotRange PLAYER_INV = makeSlotRange("inventory", 0, 36);

    public static SlotRange makeSlotRange(String name, int startValue, int size) {
        IntList intlist = new IntArrayList(size);

        for (int i = 0; i < size; i++) {
            int j = startValue + i;
            intlist.add(j);
        }

        return SlotRange.of(name, intlist);
    }
}
