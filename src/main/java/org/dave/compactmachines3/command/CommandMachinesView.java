package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.dave.compactmachines3.CompactMachines3;
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

        int coords = WorldSavedDataMachines.INSTANCE.nextCoord-1;
        if(args.length == 1) {
            coords = Integer.parseInt(args[0]);
        }

        if(coords < 0 || coords >= WorldSavedDataMachines.INSTANCE.nextCoord) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)sender.getCommandSenderEntity();

        player.openGui(CompactMachines3.instance, GuiIds.MACHINE_ADMIN.ordinal(), player.world, 0,0,0);
        PackageHandler.instance.sendTo(new MessageMachineContent(coords), player);
    }
}
