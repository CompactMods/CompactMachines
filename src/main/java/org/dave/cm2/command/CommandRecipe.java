package org.dave.cm2.command;

public class CommandRecipe extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandRecipeUnpackDefaults());
    }

    @Override
    public String getCommandName() {
        return "recipe";
    }
}