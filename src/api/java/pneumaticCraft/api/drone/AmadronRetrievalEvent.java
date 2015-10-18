package pneumaticCraft.api.drone;

import pneumaticCraft.api.PneumaticRegistry;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired (on the MinecraftForge.EVENT_BUS) when {@link PneumaticRegistry.IPneumaticCraftInterface#retrieveItemsAmazonStyle(net.minecraft.world.World, int, int, int, net.minecraft.item.ItemStack...) has successfully retrieved the items requested.
 * The same drone will be passed as the one returned in the retrieve method.
 */
public class AmadronRetrievalEvent extends Event{
    public final IDrone drone;

    public AmadronRetrievalEvent(IDrone drone){
        this.drone = drone;
    }
}
