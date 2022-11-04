package dev.compactmods.machines.machine.exceptions;

public class NonexistentMachineException extends Throwable {
    private final int machine;

    public NonexistentMachineException(int machine) {
        this.machine = machine;
    }

    public int getMachine() {
        return machine;
    }
}
