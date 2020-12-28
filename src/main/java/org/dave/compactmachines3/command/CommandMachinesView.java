package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.network.MessageMachineChunk;
import org.dave.compactmachines3.network.MessageMachineContent;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.reference.GuiIds;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

public class CommandMachinesView extends CommandBaseExt {
    @Override
    public String getName() {
        return "view";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return;
        }

        int id = WorldSavedDataMachines.getInstance().nextId - 1;
        if(args.length == 1) {
            id = Integer.parseInt(args[0]);
        }

        if(id < 0 || id >= WorldSavedDataMachines.getInstance().nextId) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)sender.getCommandSenderEntity();

        player.openGui(CompactMachines3.instance, GuiIds.MACHINE_ADMIN.ordinal(), player.world, 0,0,0);
        PackageHandler.instance.sendTo(new MessageMachineContent(id), player);
        PackageHandler.instance.sendTo(new MessageMachineChunk(id), player);
    }
}
