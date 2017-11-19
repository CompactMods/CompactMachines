package org.dave.compactmachines3.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.item.ItemPersonalShrinkingDevice;
import org.dave.compactmachines3.item.ItemRedstoneTunnelTool;
import org.dave.compactmachines3.item.ItemTunnelTool;

public class Itemss {
    @GameRegistry.ObjectHolder("compactmachines3:psd")
    public static ItemPersonalShrinkingDevice psd;

    @GameRegistry.ObjectHolder("compactmachines3:tunneltool")
    public static ItemTunnelTool tunnelTool;

    @GameRegistry.ObjectHolder("compactmachines3:redstonetunneltool")
    public static ItemRedstoneTunnelTool redstoneTunnelTool;

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        psd.initModel();
        tunnelTool.initModel();
        redstoneTunnelTool.initModel();
    }
}
