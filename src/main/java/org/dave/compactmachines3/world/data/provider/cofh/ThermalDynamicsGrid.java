package org.dave.compactmachines3.world.data.provider.cofh;


import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import org.dave.compactmachines3.world.data.provider.AbstractExtraTileDataProvider;
import org.dave.compactmachines3.world.data.provider.ExtraTileDataProvider;

@ExtraTileDataProvider(mod = "thermaldynamics")
public class ThermalDynamicsGrid extends AbstractExtraTileDataProvider {
    @Override
    public String getName() {
        return "ThermalDynamicsGrid";
    }

    @Override
    public boolean worksWith(TileEntity te) {
        return te instanceof TileGrid;
    }

    @Override
    public NBTTagCompound writeExtraData(TileEntity tileEntity) {
        TileGrid teGrid = (TileGrid)tileEntity;
        NBTTagCompound result = new NBTTagCompound();

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 6; i++) {
            list.appendTag(new NBTTagInt(teGrid.getVisualConnectionType(i).ordinal()));
        }

        result.setTag("Connections", list);
        return result;
    }

    @Override
    public void readExtraData(TileEntity tileEntity, NBTTagCompound data) {
        TileGrid teGrid = (TileGrid)tileEntity;
        if (teGrid.clientConnections == null) {
            teGrid.clientConnections = new BlockDuct.ConnectionType[6];
        }
        NBTTagList list = data.getTagList("Connections", Constants.NBT.TAG_INT);
        for (int i = 0; i < list.tagCount(); i++) {
            int value = ((NBTTagInt)list.get(i)).getInt();
            teGrid.clientConnections[i] = BlockDuct.ConnectionType.class.getEnumConstants()[value];
        }
    }
}