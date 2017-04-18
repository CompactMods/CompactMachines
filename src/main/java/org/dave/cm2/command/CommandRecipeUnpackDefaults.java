package org.dave.cm2.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.dave.cm2.misc.ConfigurationHandler;

import java.io.File;

public class CommandRecipeUnpackDefaults extends CommandBaseExt {
    @Override
    public String getCommandName() {
        return "unpack-defaults";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        boolean force = args.length > 0 && args[0].equals("force");
        int extracted = ConfigurationHandler.extractJarDirectory("assets/cm2/config/recipes", new File(ConfigurationHandler.cmDirectory, "recipes"), force);

        sender.addChatMessage(new TextComponentString("Extracted " + extracted + " recipes to the config folder"));
    }
}
