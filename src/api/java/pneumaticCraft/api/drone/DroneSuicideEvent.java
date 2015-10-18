package pneumaticCraft.api.drone;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Event called on the MinecraftForge.EVENT_BUS just before a Drone executes a Suicide piece. Used internally by PneumaticCraft to handle Amadron requests.
 */
public class DroneSuicideEvent extends Event{
    public final IDrone drone;

    public DroneSuicideEvent(IDrone drone){
        this.drone = drone;
    }
}
