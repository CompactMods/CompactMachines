package org.dave.CompactMachines.integration.gas;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

import org.dave.CompactMachines.handler.ConfigurationHandler;

public class ExtendedGasTank extends GasTank {
	public ExtendedGasTank() {
		super(ConfigurationHandler.capacityGas);
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

	@Override
	public int getMaxGas() {
		return ConfigurationHandler.capacityGas;
	}
}
