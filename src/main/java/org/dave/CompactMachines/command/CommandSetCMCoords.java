package org.dave.CompactMachines.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

import org.dave.CompactMachines.item.ItemBlockMachine;

public class CommandSetCMCoords extends CommandBase {

	@Override
	public String getCommandName() {
		return "setCMCoords";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "command.cm.setCMCoords.usage";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] args) {

		if (args.length < 1) {
			throw new WrongUsageException("command.cm.setCMCoords.usage");
		}

		int coords = -1;
		try {
			coords = Integer.parseInt(args[0]);
		} catch (Exception e) {
			throw new WrongUsageException("command.cm.setCMCoords.usage");
		}

		ItemStack itemStack = ((EntityPlayer) commandSender).getCurrentEquippedItem();
		if (itemStack == null || !(itemStack.getItem() instanceof ItemBlockMachine)) {
			throw new WrongUsageException("command.cm.setCMCoords.noblock");
		}

		if(!itemStack.hasTagCompound()) {
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setInteger("coords", coords);
		commandSender.addChatMessage(new ChatComponentTranslation("command.cm.setCMCoords.success"));
	}

}
