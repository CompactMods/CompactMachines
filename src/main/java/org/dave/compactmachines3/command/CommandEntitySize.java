package org.dave.compactmachines3.command;

public class CommandEntitySize extends CommandMenu {


    @Override
    public String getName() {
        return "entity-size";
    }

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandEntitySizeReset());
        this.addSubcommand(new CommandEntitySizeSet());
    }
}
