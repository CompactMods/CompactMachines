package dev.compactmods.machines.api.location;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record SidedPosition(Direction side, BlockPos position) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SidedPosition that = (SidedPosition) o;
        return side == that.side && Objects.equals(this.position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(side, position);
    }
}
