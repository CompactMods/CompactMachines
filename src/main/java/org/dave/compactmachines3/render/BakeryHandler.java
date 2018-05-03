package org.dave.compactmachines3.render;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BakeryHandler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void modelBake(ModelBakeEvent event) {
        TESRFieldProjector.prebakeModel();
    }
}
