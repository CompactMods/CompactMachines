package org.dave.cm2.init;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.item.ItemMiniFluidDrop;
import org.dave.cm2.item.ItemPersonalShrinkingDevice;
import org.dave.cm2.item.ItemTunnelTool;

public class Itemss {
    public static Item psd;
    public static Item tunnelTool;
    public static Item miniFluidDrop;

    public static void init() {
        psd = new ItemPersonalShrinkingDevice().setUnlocalizedName("psd").setRegistryName(CompactMachines2.MODID, "psd");
        tunnelTool = new ItemTunnelTool().setUnlocalizedName("tunneltool").setRegistryName(CompactMachines2.MODID, "tunneltool");
        miniFluidDrop = new ItemMiniFluidDrop().setUnlocalizedName("minifluiddrop").setRegistryName(CompactMachines2.MODID, "minifluiddrop");

        registerItems();
    }

    private static void registerItems() {
        GameRegistry.register(psd);
        GameRegistry.register(tunnelTool);
        GameRegistry.register(miniFluidDrop);
    }
}
