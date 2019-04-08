package org.dave.compactmachines3.skyworld;

public enum EnumSkyWorldSize {
    SMALL(4,4),
    MEDIUM(8,8),
    LARGE(16,16);

    public int rows;
    public int cols;

    EnumSkyWorldSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }
}
