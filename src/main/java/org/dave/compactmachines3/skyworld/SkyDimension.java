package org.dave.compactmachines3.skyworld;

public class SkyDimension {
    public static SkyWorldType worldType;

    public static void init() {
        worldType = new SkyWorldType();

        //GameRegistry.registerWorldGenerator(new HexagonWorldGenerator(), 1000);
    }

}
