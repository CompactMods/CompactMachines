package pneumaticCraft.api.universalSensor;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

public interface IBlockAndCoordinatePollSensor{
    /**
     * See {@link ISensorSetting#getSensorPath()}
     * @return
     */
    public String getSensorPath();

    /**
     * See {@link ISensorSetting#needsTextBox()}
     * @return
     */
    public boolean needsTextBox();

    /**
     * See {@link ISensorSetting#needsSlot()}
     * @return
     */
    public Rectangle needsSlot();

    /**
     * See {@link ISensorSetting#getDescription()}
     * @return
     */
    public List<String> getDescription();

    /**
     * See {@link IPollSensorSetting#getRedstoneValue(World, int, int, int, int, String)} , but this has the GPS tracked coordinates
     * as extra parameters. This method will only invoke with a valid GPS tool, and when all the coordinates are within range.
     * @param world
     * @param x
     * @param y
     * @param z
     * @param sensorRange
     * @param textBoxText
     * @param positions When only one GPS Tool is inserted this contains the position of just that tool. If two GPS Tools are inserted, These are both corners of a box, and every coordinate in this box is added to the positions argument.
     * @return
     */
    public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText, Set<ChunkPosition> positions);

    /**
     * See {@link IPollSensorSetting#getPollFrequency(TileEntity)}
     * @return
     */
    public int getPollFrequency();

    /**
     * Called by GuiScreen#drawScreen this method can be used to render additional things like status/info text.
     * @param fontRenderer
     */
    public void drawAdditionalInfo(FontRenderer fontRenderer);

}
