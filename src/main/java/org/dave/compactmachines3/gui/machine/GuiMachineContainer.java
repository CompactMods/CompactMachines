package org.dave.compactmachines3.gui.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.network.MessageMachineChunk;
import org.dave.compactmachines3.network.MessageMachineContent;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.tile.TileEntityMachine;

public class GuiMachineContainer extends Container {
    World world;
    BlockPos pos;
    EntityPlayer player;

    public GuiMachineContainer(World world, BlockPos pos, EntityPlayer player) {
        this.world = world;
        this.pos = pos;
        this.player = player;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if(ConfigurationHandler.MachineSettings.autoUpdateRate <= 0) {
            return;
        }

        if (this.world.isRemote || this.world.getTotalWorldTime() % ConfigurationHandler.MachineSettings.autoUpdateRate != 0) {
            return;
        }

        TileEntity te = this.world.getTileEntity(this.pos);

        if(te == null || !(te instanceof TileEntityMachine)) {
            return;
        }

        TileEntityMachine machine = (TileEntityMachine) te;
        int id = machine.id;

        for(IContainerListener listener : this.listeners) {
            if (!(listener instanceof EntityPlayerMP)) {
                continue;
            }

            PackageHandler.instance.sendTo(new MessageMachineContent(id), (EntityPlayerMP) listener);
            PackageHandler.instance.sendTo(new MessageMachineChunk(id), (EntityPlayerMP) listener);
        }
    }
}
