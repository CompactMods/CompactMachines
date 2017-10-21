package org.dave.compactmachines3.command;

import net.minecraft.entity.player.EntityPlayer;

public class CommandMachines extends CommandMenu {
    @Override
    public void initEntries() {
        this.addSubcommand(new CommandMachinesView());
        this.addSubcommand(new CommandMachinesGive());
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return isOp;
    }

    @Override
    public String getName() {
        return "machines";
    }
}
