package pneumaticCraft.api.client.pneumaticHelmet;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired when a helmet Block Tracker is about to track an inventory. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 * @author MineMaarten
 */
@Cancelable
public class InventoryTrackEvent extends Event{
    private final TileEntity inventory;

    public InventoryTrackEvent(TileEntity inventory){
        this.inventory = inventory;
    }

    public TileEntity getTileEntity(){
        return inventory;
    }

    public IInventory getInventory(){
        return (IInventory)inventory;
    }
}
