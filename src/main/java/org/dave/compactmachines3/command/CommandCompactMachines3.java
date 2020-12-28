package org.dave.compactmachines3.command;

public class CommandCompactMachines3 extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandSchema());
        this.addSubcommand(new CommandRecipe());
        this.addSubcommand(new CommandMachines());
        this.addSubcommand(new CommandDocumentation());
    }

    @Override
    public String getName() {
        return "compactmachines3";
    }
}
