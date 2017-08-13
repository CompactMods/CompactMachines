package org.dave.compactmachines3.miniaturization;

public class FieldTools {
    public static int MAX_FIELD_DISTANCE = 6;

    public static int getFieldDimensionsAtDistance(int distance) {
        if(distance <= 0) return 0;
        if(distance > FieldTools.MAX_FIELD_DISTANCE) return 0;

        return distance * 2 - 1;
    }

}
