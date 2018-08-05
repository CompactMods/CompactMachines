package org.dave.compactmachines3.schema;

import org.dave.compactmachines3.reference.EnumMachineSize;

import java.util.List;

public class Schema {
    private String name;
    private String description;
    private List<BlockInformation> blocks;
    private EnumMachineSize size;
    private double[] spawnPosition;

    public Schema(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BlockInformation> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockInformation> blocks) {
        this.blocks = blocks;
    }

    public EnumMachineSize getSize() {
        return size;
    }

    public void setSize(EnumMachineSize size) {
        this.size = size;
    }

    public void setSpawnPosition(double[] spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public double[] getSpawnPosition() {
        return spawnPosition;
    }
}
