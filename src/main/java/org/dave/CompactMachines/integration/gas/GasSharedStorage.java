package org.dave.CompactMachines.integration.gas;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.integration.gas.ExtendedGasTank;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

public class GasSharedStorage extends AbstractSharedStorage implements IGasHandler {
    // I just pulled this number out of my arse, no idea if it's appropriate ^_^
    private static final int MAX_GAS = 1000;
    private ExtendedGasTank tank;

    public GasSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
        super(storageHandler, coord, side);

        tank = new ExtendedGasTank(MAX_GAS) {
            public void onGasChanged() {
                setDirty();
            }
        };

		max_cooldown = ConfigurationHandler.cooldownGas;
    }

    @Override
    public int receiveGas(ForgeDirection side, GasStack stack) {
        // XXX: Is always passing true for doReceive correct?
        return tank.receive(stack, true);
    }

    @Override
    public GasStack drawGas(ForgeDirection side, int amount) {
        // XXX: Is always passing true for doDraw correct?
        return tank.draw(amount, true);
    }

    @Override
    public boolean canReceiveGas(ForgeDirection side, Gas gas) {
        return tank.canReceive(gas);
    }

    @Override
    public boolean canDrawGas(ForgeDirection side, Gas gas) {
        return tank.canDraw(gas);
    }

    @Override
    public String type() {
        return "gas";
    }

    @Override
    public NBTTagCompound saveToTag() {
        NBTTagCompound compound = prepareTagCompound();
        compound.setTag("tank", tank.write(new NBTTagCompound()));

        return compound;
    }

    @Override
    public void loadFromTag(NBTTagCompound tag) {
        loadHoppingModeFromCompound(tag);

        tank.read(tag.getCompoundTag("tank"));
    }

    private void hopToTileEntity(TileEntity te, boolean opposite) {
        GasStack stack = tank.getGas();

        if (stack == null || stack.amount == 0) {
            return;
        }

        stack = stack.copy();

        if (cooldown == max_cooldown) {
            cooldown = 0;
        } else {
            cooldown++;
            return;
        }

        if (te instanceof IGasHandler) {
            IGasHandler gh = (IGasHandler) te;

            ForgeDirection hoppingSide = ForgeDirection.getOrientation(side);

            if (opposite) {
                hoppingSide = hoppingSide.getOpposite();
            }

            if (gh.canReceiveGas(hoppingSide, stack.getGas())) {
                int received = gh.receiveGas(hoppingSide, stack);

                if (received > 0) {
                    this.tank.draw(received, true);

                    te.markDirty();
                }
            }

        }

    }

    @Override
    public void hopToOutside(TileEntityMachine te, TileEntity outside) {
        hopToTileEntity(outside, true);
    }

    @Override
    public void hopToInside(TileEntityInterface te, TileEntity inside) {
        hopToTileEntity(inside, false);
    }
}
