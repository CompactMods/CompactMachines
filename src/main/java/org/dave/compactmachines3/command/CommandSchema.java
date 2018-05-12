package org.dave.compactmachines3.command;

public class CommandSchema extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandSchemaSave());
        this.addSubcommand(new CommandSchemaLoad());
        this.addSubcommand(new CommandSchemaSet());
        this.addSubcommand(new CommandSchemaReloadFiles());
    }

    @Override
    public String getName() {
        return "schema";
    }
}
