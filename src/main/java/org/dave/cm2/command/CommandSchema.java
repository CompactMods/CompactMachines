package org.dave.cm2.command;

import net.minecraft.entity.player.EntityPlayer;

public class CommandSchema extends CommandMenu {

    @Override
    public void initEntries() {
        this.addSubcommand(new CommandSchemaSave());
        this.addSubcommand(new CommandSchemaLoad());
    }

    @Override
    public String getCommandName() {
        return "schema";
    }
}
