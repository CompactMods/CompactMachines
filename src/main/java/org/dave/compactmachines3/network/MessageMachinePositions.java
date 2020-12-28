package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

import java.util.HashMap;
import java.util.Map;

public class MessageMachinePositions implements IMessage, IMessageHandler<MessageMachinePositions, IMessage> {
    Map<Integer, BlockPos> machineGrid;
    Map<Integer, EnumMachineSize> machineSizes;

    public MessageMachinePositions() {}

    public MessageMachinePositions(Map<Integer, BlockPos> machineGrid, Map<Integer, EnumMachineSize> machineSizes) {
        this.machineGrid = machineGrid;
        this.machineSizes = machineSizes;
    }

    public static MessageMachinePositions initWithWorldSavedData() {
        return new MessageMachinePositions(WorldSavedDataMachines.getInstance().machineGrid, WorldSavedDataMachines.getInstance().machineSizes);
    }

    public static void updateClientMachinePositions() {
        for (EntityPlayerMP p : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            MessageMachinePositions message = MessageMachinePositions.initWithWorldSavedData();
            PackageHandler.instance.sendTo(message, p);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        NBTTagCompound machineGridTag = tag.getCompoundTag("machineGrid");
        NBTTagCompound machineSizesTag = tag.getCompoundTag("machineSizes");
        this.machineGrid = new HashMap<>();
        for (String key : machineGridTag.getKeySet()) {
            machineGrid.put(Integer.parseInt(key), NBTUtil.getPosFromTag(machineGridTag.getCompoundTag(key)));
        }
        this.machineSizes = new HashMap<>();
        for (String key : machineSizesTag.getKeySet()) {
            machineSizes.put(Integer.parseInt(key), EnumMachineSize.getFromMeta(machineSizesTag.getInteger(key)));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound machineGridTag = new NBTTagCompound();
        NBTTagCompound machineSizesTag = new NBTTagCompound();
        for (Map.Entry<Integer, BlockPos> entry : machineGrid.entrySet()) {
            machineGridTag.setTag(entry.getKey().toString(), NBTUtil.createPosTag(entry.getValue()));
        }
        for (Map.Entry<Integer, EnumMachineSize> entry : machineSizes.entrySet()) {
            machineSizesTag.setInteger(entry.getKey().toString(), entry.getValue().getMeta());
        }
        tag.setTag("machineGrid", machineGridTag);
        tag.setTag("machineSizes", machineSizesTag);
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    public IMessage onMessage(MessageMachinePositions message, MessageContext ctx) {
        CompactMachines3.clientMachineGrid = message.machineGrid;
        CompactMachines3.clientMachineSizes = message.machineSizes;
        return null;
    }
}
