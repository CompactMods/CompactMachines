package org.dave.compactmachines3.schema;

import net.minecraft.util.math.Vec3d;
import org.dave.compactmachines3.reference.EnumMachineSize;

import java.util.List;

public class Schema {
    private String name;
    private String description;
    private List<BlockInformation> blocks;
    private EnumMachineSize size;
    private Vec3d spawnPosition;

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

    public void setSpawnPosition(Vec3d spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public Vec3d getSpawnPosition() {
        return spawnPosition;
    }
}
