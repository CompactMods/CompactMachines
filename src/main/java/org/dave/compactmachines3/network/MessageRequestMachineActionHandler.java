package org.dave.compactmachines3.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

public class MessageRequestMachineActionHandler implements IMessageHandler<MessageRequestMachineAction, MessageMachineContent> {
    @Override
    public MessageMachineContent onMessage(MessageRequestMachineAction message, MessageContext ctx) {
        int coords = message.coords;
        if(message.coords < 0) {
            coords = WorldSavedDataMachines.INSTANCE.nextCoord-1;
        }
        if(message.coords >= WorldSavedDataMachines.INSTANCE.nextCoord) {
            coords = 0;
        }

        int finalCoords = coords;
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        if(message.action == MessageRequestMachineAction.Action.GIVE_ITEM) {
            EnumMachineSize size = WorldSavedDataMachines.INSTANCE.machineSizes.get(coords);

            serverPlayer.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = new ItemStack(Blockss.machine, 1, size.getMeta());
                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("coords", finalCoords);
                stack.setTagCompound(compound);

                ItemHandlerHelper.giveItemToPlayer(serverPlayer, stack);
                WorldSavedDataMachines.INSTANCE.removeMachinePosition(finalCoords);
            });
        }

        serverPlayer.getServerWorld().addScheduledTask(() -> {
            PackageHandler.instance.sendTo(new MessageMachineContent(finalCoords), serverPlayer);
        });

        return null;
    }
}
