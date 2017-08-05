package org.dave.compactmachines3.schema;

import java.util.List;

public class Schema {
    public String name;
    public List<BlockInformation> blocks;

    public Schema(String name, List<BlockInformation> blocks) {
        this.name = name;
        this.blocks = blocks;
    }
}
