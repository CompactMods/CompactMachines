package pneumaticCraft.api.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.api.IHeatExchangerLogic;

/**
 * Extend this class, and register it via {@link PneumaticRegistry.getInstance().registerHeatBehaviour()}
 * This can be used to add heat dependent logic to non-TE's or blocks you don't have access to. PneumaticCraft uses this to power Furnaces with heat,
 * and to turn Lava into Obsidian when heat is drained. This only works for ticking heat logic, so not for static heat sources like lava blocks.
 */
public abstract class HeatBehaviour<Tile extends TileEntity> {

    private IHeatExchangerLogic connectedHeatLogic;
    private World world;
    private int x, y, z;
    private Tile cachedTE;
    private Block block;

    /**
     * Called by the connected IHeatExchangerLogic.
     * @param connectedHeatLogic
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, int x, int y, int z){
        this.connectedHeatLogic = connectedHeatLogic;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        cachedTE = null;
        block = null;
    }

    public IHeatExchangerLogic getHeatExchanger(){
        return connectedHeatLogic;
    }

    public World getWorld(){
        return world;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getZ(){
        return z;
    }

    public Tile getTileEntity(){
        if(cachedTE == null || cachedTE.isInvalid()) cachedTE = (Tile)world.getTileEntity(x, y, z);
        return cachedTE;
    }

    public Block getBlock(){
        if(block == null) block = world.getBlock(x, y, z);
        return block;
    }

    /**
     * Unique id for this behaviour. Used in NBT saving. I recommend prefixing it with your modid.
     * @return
     */
    public abstract String getId();

    /**
     * Return true when this heat behaviour is applicable for this coordinate. World access methods can be used here (getWorld(), getX(), getY(), getZ(), getBlock(), getTileEntity()).
     * @return
     */
    public abstract boolean isApplicable();

    /**
     * Called every tick to update this behaviour.
     */
    public abstract void update();

    public void writeToNBT(NBTTagCompound tag){
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setInteger("z", z);
    }

    public void readFromNBT(NBTTagCompound tag){
        x = tag.getInteger("x");
        y = tag.getInteger("y");
        z = tag.getInteger("z");
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof HeatBehaviour) {
            HeatBehaviour behaviour = (HeatBehaviour)o;
            return behaviour.getId().equals(getId()) && behaviour.getX() == getX() && behaviour.getY() == getY() && behaviour.getZ() == getZ();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        int i = getId().hashCode();
        i = i * 31 + getX();
        i = i * 31 + getY();
        i = i * 31 + getZ();
        return i;
    }
}
