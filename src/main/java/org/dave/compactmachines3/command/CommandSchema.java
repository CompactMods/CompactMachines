package org.dave.compactmachines3.command;

public class CommandSchema extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandSchemaSave());
        this.addSubcommand(new CommandSchemaLoad());
    }

    @Override
    public String getName() {
        return "schema";
    }
}
