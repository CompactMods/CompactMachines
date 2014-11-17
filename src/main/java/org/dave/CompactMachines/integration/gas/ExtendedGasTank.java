package org.dave.CompactMachines.integration.gas;

import net.minecraftforge.common.util.ForgeDirection;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

public class ExtendedGasTank extends GasTank {
    public ExtendedGasTank(int capacity) {
        super(capacity);
    }

    public void onGasChanged() {};

    @Override
    public GasStack draw(int amount, boolean doDraw) {
        GasStack drawn = super.draw(amount, doDraw);

        if (doDraw && drawn != null && drawn.amount > 0) {
            onGasChanged();
        }
        
        return drawn;
    }

    @Override
    public int receive(GasStack stack, boolean doReceive) {
        int received = super.receive(stack, doReceive);

        if (doReceive && received > 0) {
            onGasChanged();
        }

        return received;
    }
}
