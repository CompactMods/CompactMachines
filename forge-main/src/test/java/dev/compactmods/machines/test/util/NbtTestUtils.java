package dev.compactmods.machines.test.util;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NbtTestUtils {
    public static boolean checkListSize(CompoundTag tag, String list, int expectedSize) {
        return checkListSize(tag, list, Tag.TAG_COMPOUND, expectedSize);
    }

    public static boolean checkListSize(CompoundTag tag, String list, int listType, int expectedSize) {
        if(!tag.contains(list, Tag.TAG_LIST))
            return false;

        int actualSize = tag.getList(list, listType).size();
        if(actualSize != expectedSize)
            throw new GameTestAssertException("Expected NBT list %s to have %s elements; got %s".formatted(list, expectedSize, actualSize));

        return true;
    }
}
