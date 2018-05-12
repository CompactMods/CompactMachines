package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.dave.compactmachines3.schema.SchemaRegistry;

public class CommandSchemaReloadFiles extends CommandBaseExt {
    @Override
    public String getName() {
        return "reload-files";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        SchemaRegistry.init();
    }
}
