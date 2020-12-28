package org.dave.compactmachines3.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        this.commands.add(subcommand.getName());
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        boolean found = false;
        if(args.length > 0) {
            for(CommandBaseExt cmd : subcommands) {
                if(cmd.getName().equalsIgnoreCase(args[0])) {
                    found = true;
                    if(cmd.checkPermission(server, sender)) {
                        String[] remaining = Arrays.copyOfRange(args, 1, args.length);
                        cmd.execute(server, sender, remaining);
                        return;
                    }
                }
            }
        }

        if (found) {
            sender.sendMessage(new TextComponentTranslation("commands.compactmachines3.denied"));
            return;
        }

        TextComponentTranslation tc = new TextComponentTranslation("commands.compactmachines3.available");
        tc.getStyle().setUnderlined(true);
        tc.appendText("\n");

        for (CommandBaseExt cmd : subcommands) {
            boolean allowed = cmd.checkPermission(server, sender);
            String color = "" + (allowed ? TextFormatting.GREEN : TextFormatting.DARK_RED);
            tc.appendSibling(new TextComponentString("\n" + color + cmd.getName() + " "));
            TextComponentTranslation tt = new TextComponentTranslation(cmd.getCommandDescription(sender));
            tt.getStyle().setColor(TextFormatting.GRAY);
            tt.getStyle().setUnderlined(false);
            tc.appendSibling(tt);
            tc.appendSibling(new TextComponentString("" + TextFormatting.RESET));
        }

        sender.sendMessage(tc);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, commands);
        } else if(args.length > 1) {
            for(CommandBase cmd : subcommands) {
                if (cmd.getName().equalsIgnoreCase(args[0]) && cmd.checkPermission(server, sender)) {
                    String[] remaining = Arrays.copyOfRange(args, 1, args.length);
                    return cmd.getTabCompletions(server, sender, remaining, pos);
                }
            }
        }

        return super.getTabCompletions(server, sender, args, pos);
    }
}
