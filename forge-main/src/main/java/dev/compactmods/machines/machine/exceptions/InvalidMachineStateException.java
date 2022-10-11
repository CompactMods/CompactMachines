package dev.compactmods.machines.machine.exceptions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.antlr.v4.runtime.atn.BlockStartState;

public class InvalidMachineStateException extends Throwable {
    private final BlockPos machinePosition;
    private final BlockState state;

    public InvalidMachineStateException(BlockPos machinePos, BlockState state) {
        super("Invalid machine state.");
        this.machinePosition = machinePos;
        this.state = state;
    }

    public InvalidMachineStateException(BlockPos machinePos, BlockState state, String s) {
        super(s);
        this.machinePosition = machinePos;
        this.state = state;
    }

    public BlockPos getMachinePosition() {
        return machinePosition;
    }

    public BlockState getState() {
        return state;
    }
}
