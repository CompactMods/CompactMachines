package org.dave.CompactMachines.integration.appeng;

import java.util.EnumSet;

import org.dave.CompactMachines.tileentity.TileEntityCM;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;

public class CMGridBlock implements IGridBlock {
	protected TileEntityCM gridHost;

	public CMGridBlock(TileEntityCM gridHost) {
		this.gridHost = gridHost;
	}

	@Override
	public double getIdlePowerUsage() {
		return 0;
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return EnumSet.of(GridFlags.DENSE_CAPACITY);
	}

	@Override
	public boolean isWorldAccessable() {
		return false;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(gridHost);
	}

	@Override
	public AEColor getGridColor() {
		return AEColor.Transparent;
	}

	@Override
	public void onGridNotification(GridNotification notification) {
	}

	@Override
	public void setNetworkStatus(IGrid grid, int channelsInUse) {
	}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides() {
		return EnumSet.of(
				ForgeDirection.DOWN, ForgeDirection.UP,
				ForgeDirection.WEST, ForgeDirection.EAST,
				ForgeDirection.NORTH, ForgeDirection.SOUTH
		);
	}

	@Override
	public IGridHost getMachine() {
		return (IGridHost) gridHost;
	}

	@Override
	public void gridChanged() {
	}

	@Override
	public ItemStack getMachineRepresentation() {
		return null;
	}

}
