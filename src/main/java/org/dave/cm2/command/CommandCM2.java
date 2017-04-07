package org.dave.cm2.command;

public class CommandCM2 extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandSchema());
        this.addSubcommand(new CommandEntitySize());
    }

    @Override
    public String getCommandName() {
        return "cm2";
    }
}
