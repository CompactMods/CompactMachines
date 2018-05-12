package org.dave.compactmachines3.schema;

import org.dave.compactmachines3.reference.EnumMachineSize;

import java.util.List;

public class Schema {
    public String name;
    public List<BlockInformation> blocks;
    public EnumMachineSize size;

    public Schema(String name, List<BlockInformation> blocks, EnumMachineSize size) {
        this.name = name;
        this.blocks = blocks;
        this.size = size;
    }
}
