package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;

import java.util.HashSet;
import java.util.Set;

public class MessageMachineContent implements IMessage {
    protected int machineSize;
    protected int coords;

    protected DimensionBlockPos machinePos;
    protected String owner;
    protected String customName;
    protected Set<String> playerWhiteList;
    protected boolean locked;

    public MessageMachineContent(int coords) {
        this.coords = coords;
        WorldSavedDataMachines data = WorldSavedDataMachines.INSTANCE;

        machinePos = data.getMachinePosition(coords);
        machineSize = data.machineSizes.getOrDefault(coords, EnumMachineSize.MAXIMUM).getDimension();

        if(machinePos != null) {
            TileEntity te = DimensionTools.getWorldServerForDimension(machinePos.getDimension()).getTileEntity(machinePos.getBlockPos());
            if (te != null && te instanceof TileEntityMachine) {
                TileEntityMachine machine = (TileEntityMachine) te;
                owner = machine.getOwnerName();
                customName = machine.getCustomName();
                playerWhiteList = machine.getWhiteList();
                locked = machine.isLocked();
            }
        }
    }

    public MessageMachineContent() {
    }

    public void setMachineSize(int machineSize) {
        this.machineSize = machineSize;
    }

    public void setCoords(int coords) {
        this.coords = coords;
    }

    public void setMachinePos(DimensionBlockPos machinePos) {
        this.machinePos = machinePos;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        machineSize = buf.readInt();
        coords = buf.readInt();
        boolean hasMachineBlock = buf.readBoolean();
        if(hasMachineBlock) {
            machinePos = new DimensionBlockPos(buf);
            owner = ByteBufUtils.readUTF8String(buf);
            customName = ByteBufUtils.readUTF8String(buf);
            locked = buf.readBoolean();
        }

        int whiteListSize = buf.readInt();
        playerWhiteList = new HashSet<>();
        for(int i = 0; i < whiteListSize; i++) {
            String name = ByteBufUtils.readUTF8String(buf);

            playerWhiteList.add(name);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(machineSize);
        buf.writeInt(coords);
        if(machinePos != null) {
            buf.writeBoolean(true);
            machinePos.writeToByteBuf(buf);
            String owner = this.owner == null ? "" : this.owner;
            String customName = this.customName == null ? "" : this.customName;
            ByteBufUtils.writeUTF8String(buf, owner);
            ByteBufUtils.writeUTF8String(buf, customName);
            buf.writeBoolean(locked);
        } else {
            buf.writeBoolean(false);
        }

        if(playerWhiteList == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(playerWhiteList.size());
            for (String playerName : playerWhiteList) {
                ByteBufUtils.writeUTF8String(buf, playerName);
            }
        }
    }
}
