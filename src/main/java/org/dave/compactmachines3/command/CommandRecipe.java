package org.dave.compactmachines3.command;

public class CommandRecipe extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandRecipeUnpackDefaults());
        this.addSubcommand(new CommandRecipeCopyShape());
    }

    @Override
    public String getName() {
        return "recipe";
    }
}