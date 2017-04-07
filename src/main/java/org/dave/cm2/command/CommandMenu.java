package org.dave.cm2.command;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CommandMenu extends CommandBaseExt {
    private List<CommandBaseExt> subcommands = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    public CommandMenu() {
        initEntries();
    }

    public abstract void initEntries();

    public void addSubcommand(CommandBaseExt subcommand) {
        if(subcommand.getParentCommand() != null) {
            return;
        }

        subcommand.setParentCommand(this);
        this.subcommands.add(subcommand);
        this.commands.add(subcommand.getCommandName());
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.cm2.usage";
    }

    @Override
    public String getCommandSuffix() {
        return I18n.format("commands.cm2.menu.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        boolean found = false;
        if(args.length > 0) {
            for(CommandBase cmd : subcommands) {
                if(cmd.getCommandName().equalsIgnoreCase(args[0]) && cmd.checkPermission(server, sender)) {
                    found = true;
                    String[] remaining = Arrays.copyOfRange(args, 1, args.length);
                    cmd.execute(server, sender, remaining);
                }
            }
        }

        if(args.length == 0 || found == false) {
            String commandList = subcommands.stream().map(c -> "\n - " + c.getCommandName() + " " + c.getCommandSuffix()).collect(Collectors.joining());
            sender.addChatMessage(new TextComponentString(I18n.format("commands.cm2.available", this.getCommandPrefix()) + commandList));
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, commands);
        } else if(args.length > 1) {
            for(CommandBase cmd : subcommands) {
                if (cmd.getCommandName().equalsIgnoreCase(args[0]) && cmd.checkPermission(server, sender)) {
                    String[] remaining = Arrays.copyOfRange(args, 1, args.length);
                    return cmd.getTabCompletionOptions(server, sender, remaining, pos);
                }
            }
        }

        return super.getTabCompletionOptions(server, sender, args, pos);
    }
}
