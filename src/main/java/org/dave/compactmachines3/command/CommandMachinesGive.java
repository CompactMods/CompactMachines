package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.items.ItemHandlerHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

public class CommandMachinesGive extends CommandBaseExt {
    @Override
    public String getName() {
        return "give";
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

        if(args.length != 1) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        int id = Integer.parseInt(args[0]);
        if(id < 0 || id >= WorldSavedDataMachines.getInstance().nextId) {
            return;
        }

        EnumMachineSize size = WorldSavedDataMachines.getInstance().machineSizes.get(id);

        ItemStack stack = new ItemStack(Blockss.machine, 1, size.getMeta());
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("machineId", id);
        stack.setTagCompound(compound);

        ItemHandlerHelper.giveItemToPlayer(player, stack);
        WorldSavedDataMachines.getInstance().removeMachinePosition(id);
    }
}
