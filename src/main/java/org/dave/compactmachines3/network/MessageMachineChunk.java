package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.utility.ChunkUtils;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;

public class MessageMachineChunk implements IMessage, IMessageHandler<MessageMachineChunk, IMessage> {
    protected int id;
    protected NBTTagCompound data;

    public MessageMachineChunk() {
    }

    public MessageMachineChunk(int id) {
        this.id = id;
        BlockPos roomPos = WorldSavedDataMachines.getInstance().getMachineRoomPosition(this.id);
        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if (roomPos == null || machineWorld == null) {
            this.data = new NBTTagCompound();
        } else {
            Chunk chunk = machineWorld.getChunk(roomPos);
            this.data = ChunkUtils.writeChunkToNBT(chunk, machineWorld, new NBTTagCompound());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        try {
            data = ByteBufUtils.readTag(buf);
        } catch (Exception e) {
            CompactMachines3.logger.debug("Unable to read nbt data from buffer: {}", e.getMessage());
            data = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        ByteBufUtils.writeTag(buf, data);
    }

    @Override
    public IMessage onMessage(MessageMachineChunk message, MessageContext ctx) {
        if(!CompactMachines3.clientWorldData.isInitialized()) {
            return null;
        }

        // TODO: Clients might need to update the rendering of the machine block and its neighbors
        CompactMachines3.clientWorldData.worldClone.providerClient.loadChunkFromNBT(message.data);
        return null;
    }
}
