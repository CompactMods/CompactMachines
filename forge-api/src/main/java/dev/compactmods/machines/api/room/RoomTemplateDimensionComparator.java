package dev.compactmods.machines.api.room;

import java.util.Comparator;

public class RoomTemplateDimensionComparator implements Comparator<RoomTemplate> {
    @Override
    public int compare(RoomTemplate o1, RoomTemplate o2) {
        final var dim1 = o1.dimensions().getX() * o1.dimensions().getY() * o1.dimensions().getZ();
        final var dim2 = o2.dimensions().getX() * o2.dimensions().getY() * o2.dimensions().getZ();
        return dim1 - dim2;
    }
}
