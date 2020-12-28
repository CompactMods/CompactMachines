package org.dave.compactmachines3.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;

public class MessagePlayerWhiteListToggleHandler implements IMessageHandler<MessagePlayerWhiteListToggle, MessagePlayerWhiteListToggle> {
    @Override
    public MessagePlayerWhiteListToggle onMessage(MessagePlayerWhiteListToggle message, MessageContext ctx) {
        int id = message.id;
        if(message.id < 0 || message.id >= WorldSavedDataMachines.getInstance().nextId) {
            return null;
        }

        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

        serverPlayer.getServerWorld().addScheduledTask(() -> {
            DimensionBlockPos pos = WorldSavedDataMachines.getInstance().machinePositions.get(id);
            TileEntity te = DimensionTools.getWorldServerForDimension(pos.getDimension()).getTileEntity(pos.getBlockPos());
            if (te != null && te instanceof TileEntityMachine) {
                TileEntityMachine machine = (TileEntityMachine) te;
                if(!machine.isOnWhiteList(message.playerName)) {
                    machine.addToWhiteList(message.playerName);
                } else {
                    machine.removeFromWhiteList(message.playerName);
                }

                machine.markDirty();
            }

            PackageHandler.instance.sendTo(new MessageMachineContent(id), serverPlayer);
        });

        return null;
    }
}
