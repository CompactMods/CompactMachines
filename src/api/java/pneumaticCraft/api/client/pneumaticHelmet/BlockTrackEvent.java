package pneumaticCraft.api.client.pneumaticHelmet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired when a helmet Block Tracker is about to track a block. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 * @author MineMaarten
 */
@Cancelable
public class BlockTrackEvent extends Event{

    public final World world;
    public final int x, y, z;
    public final TileEntity te;

    public BlockTrackEvent(World world, int x, int y, int z, TileEntity te){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.te = te;
    }

}
