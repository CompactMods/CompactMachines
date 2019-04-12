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
import org.dave.compactmachines3.utility.Logz;

public class GuiMachineContainer extends Container {
    World world;
    BlockPos pos;

    public GuiMachineContainer(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
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
        int coords = machine.coords;

        for(IContainerListener listener : this.listeners) {
            if (!(listener instanceof EntityPlayerMP)) {
                continue;
            }

            PackageHandler.instance.sendTo(new MessageMachineContent(coords), (EntityPlayerMP) listener);
            PackageHandler.instance.sendTo(new MessageMachineChunk(coords), (EntityPlayerMP) listener);
        }
    }
}
