package dev.compactmods.machines.api.machine.block;

import dev.compactmods.machines.core.machine.MachineColor;

public interface ICompactMachineBlockEntity {
    MachineColor getMachineColor();

    void setMachineColor(MachineColor newColor);
}
