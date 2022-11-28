package dev.compactmods.machines.api.resource;

import dev.compactmods.machines.api.location.IDimensionalBlockPosition;

import java.util.stream.Stream;

public interface IResourceNetwork {

    IResourceType resourceType();

    Stream<IDimensionalBlockPosition> getInputs();
    Stream<IDimensionalBlockPosition> getOutputs();

}
