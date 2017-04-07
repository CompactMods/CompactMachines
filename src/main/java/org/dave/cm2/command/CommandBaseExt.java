package org.dave.cm2.command;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;

public abstract class CommandBaseExt extends CommandBase {
    public CommandBaseExt parentCommand;

    public CommandBaseExt getParentCommand() {
        return parentCommand;
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

        result += this.getCommandName();
        return result;
    }

    public String getCommandPrefix() {
        return "/" + this.getParentConcatenation(" ") + " ";
    }

    public String getCommandSuffix() {
        return I18n.format("commands." + this.getFullCommandName() + ".usage");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return this.getCommandPrefix() + this.getCommandSuffix();
    }

    public WrongUsageException getUsageException(ICommandSender sender) {
        return new WrongUsageException(this.getCommandUsage(sender));
    }

    public SyntaxErrorException getException(ICommandSender sender, String type) {
        return new SyntaxErrorException("commands." + this.getFullCommandName() + ".exception." + type);
    }
}
