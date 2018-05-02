package org.dave.compactmachines3.render;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.utility.Logz;

public class BakeryHandler {

    @SubscribeEvent
    public static void modelBake(ModelBakeEvent event) {
        TESRFieldProjector.prebakeModel();
    }
}
