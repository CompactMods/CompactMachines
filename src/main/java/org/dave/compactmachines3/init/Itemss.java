package org.dave.compactmachines3.init;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.item.ItemMiniFluidDrop;
import org.dave.compactmachines3.item.ItemPersonalShrinkingDevice;
import org.dave.compactmachines3.item.ItemTunnelTool;

public class Itemss {
    public static Item psd;
    public static Item tunnelTool;
    public static Item miniFluidDrop;

    public static void init() {
        psd = new ItemPersonalShrinkingDevice().setUnlocalizedName("psd").setRegistryName(CompactMachines3.MODID, "psd");
        tunnelTool = new ItemTunnelTool().setUnlocalizedName("tunneltool").setRegistryName(CompactMachines3.MODID, "tunneltool");
        miniFluidDrop = new ItemMiniFluidDrop().setUnlocalizedName("minifluiddrop").setRegistryName(CompactMachines3.MODID, "minifluiddrop");

        registerItems();
    }

    private static void registerItems() {
        GameRegistry.register(psd);
        GameRegistry.register(tunnelTool);
        GameRegistry.register(miniFluidDrop);
    }
}
