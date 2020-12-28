package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.dave.compactmachines3.network.MessageDocumentation;
import org.dave.compactmachines3.network.PackageHandler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandDocumentation extends CommandBaseExt {
    @Override
    public String getName() {
        return "documentation";
    }

    @Override
    public List<String> getAliases() {
        return Stream.of("docs").collect(Collectors.toList());
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        PackageHandler.instance.sendTo(new MessageDocumentation(), player);
    }
}
