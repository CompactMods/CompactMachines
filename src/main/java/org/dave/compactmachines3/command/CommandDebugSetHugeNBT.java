package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;

import java.util.Arrays;

public class CommandDebugSetHugeNBT extends CommandBaseExt {
    @Override
    public String getName() {
        return "setHugeNBT";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        ItemStack stack = player.getHeldItemMainhand().copy();
        NBTTagCompound newTag;
        if(stack.hasTagCompound()) {
            newTag = stack.getTagCompound();
        } else {
            newTag = new NBTTagCompound();
        }

        // TODO: Make size configurable via arguments
        int fourmeg = 1024*1024*4;
        int limit = 2097152;
        byte[] hugeByteArray = new byte[fourmeg];
        Arrays.fill(hugeByteArray, (byte)255);
        newTag.setByteArray("debugHugeNBTTag", hugeByteArray);

        stack.setTagCompound(newTag);

        player.setHeldItem(EnumHand.MAIN_HAND, stack);
    }
}
