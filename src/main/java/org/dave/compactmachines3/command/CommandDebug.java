package org.dave.compactmachines3.command;

import net.minecraft.entity.player.EntityPlayer;

public class CommandDebug extends CommandMenu {
    @Override
    public void initEntries() {
        this.addSubcommand(new CommandDebugSetHugeNBT());
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return isOp;
    }

    @Override
    public String getName() {
        return "debug";
    }
}
