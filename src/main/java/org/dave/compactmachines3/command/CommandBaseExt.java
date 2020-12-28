package org.dave.compactmachines3.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class CommandBaseExt extends CommandBase {
    public CommandBaseExt parentCommand;

    public CommandBaseExt getParentCommand() {
        return parentCommand;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        if(sender.getCommandSenderEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
            boolean creative = player.capabilities.isCreativeMode;
            boolean isOp = server.getPlayerList().canSendCommands(player.getGameProfile());
            return isAllowed(player, creative, isOp);
        }

        return super.checkPermission(server, sender);
    }

    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return creative && isOp;
    }

    public void setParentCommand(CommandBaseExt parentCommand) {
        this.parentCommand = parentCommand;
    }

    public String getFullCommandName() {
        return getParentConcatenation(".");
    }

    private String getParentConcatenation(String delim) {
        String result = "";
        if(this.getParentCommand() != null) {
            result += this.getParentCommand().getParentConcatenation(delim) + delim;
        }

        result += this.getName();
        return result;
    }

    public String getCommandDescription(ICommandSender sender) {
        return "commands." + this.getFullCommandName() + ".description";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands." + this.getFullCommandName() + ".usage";
    }

    public WrongUsageException getUsageException(ICommandSender sender) {
        return new WrongUsageException(this.getUsage(sender));
    }

    public SyntaxErrorException getException(ICommandSender sender, String type, Object... args) {
        return new SyntaxErrorException("commands." + this.getFullCommandName() + ".exception." + type, args);
    }
}
