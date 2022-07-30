package dev.compactmods.machines.dimension;

public class MissingDimensionException extends Throwable {
    public MissingDimensionException() {
        super("Could not find compact machine dimension. This is likely a bug; please report it.");
    }

    public MissingDimensionException(String msg) {
        super(msg);
    }
}
