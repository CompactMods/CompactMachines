package org.dave.cm2.command;

public class CommandEntitySize extends CommandMenu {


    @Override
    public String getCommandName() {
        return "entity-size";
    }

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandEntitySizeReset());
        this.addSubcommand(new CommandEntitySizeSet());
    }
}
