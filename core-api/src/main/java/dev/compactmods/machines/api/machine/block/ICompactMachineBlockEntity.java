package dev.compactmods.machines.api.machine.block;

import dev.compactmods.machines.api.machine.MachineColor;

public interface ICompactMachineBlockEntity {
    MachineColor getMachineColor();

    void setMachineColor(MachineColor newColor);
}
