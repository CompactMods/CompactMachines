package org.dave.compactmachines3.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;

public class MessageSetMachineNameHandler implements IMessageHandler<MessageSetMachineName, MessageSetMachineName> {
    @Override
    public MessageSetMachineName onMessage(MessageSetMachineName message, MessageContext ctx) {
        int id = message.id;
        if(message.id < 0 || message.id >= WorldSavedDataMachines.getInstance().nextId) {
            return null;
        }

        int finalId = id;
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

        serverPlayer.getServerWorld().addScheduledTask(() -> {
            DimensionBlockPos pos = WorldSavedDataMachines.getInstance().machinePositions.get(finalId);
            TileEntity te = DimensionTools.getWorldServerForDimension(pos.getDimension()).getTileEntity(pos.getBlockPos());
            if (te instanceof TileEntityMachine) {
                TileEntityMachine machine = (TileEntityMachine) te;
                machine.setCustomName(message.newName);
                machine.markDirty();

                IBlockState state = machine.getWorld().getBlockState(pos.getBlockPos());
                machine.getWorld().notifyBlockUpdate(pos.getBlockPos(), state, state, 3);
            }

            PackageHandler.instance.sendTo(new MessageMachineContent(finalId), serverPlayer);
        });

        return null;
    }
}
