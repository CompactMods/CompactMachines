package dev.compactmods.machines.api;

public interface ICompactMachinesAddon {

    default void prepare() {
        // no-op
    }

    default void afterRegistration() {
        // no-op
    }
}
