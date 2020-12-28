package org.dave.compactmachines3.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.gui.machine.GuiMachine;
import org.dave.compactmachines3.gui.machine.GuiMachineContainer;
import org.dave.compactmachines3.gui.psd.GuiPSDScreen;
import org.dave.compactmachines3.reference.GuiIds;

public class GuiHandler implements IGuiHandler {
    public static void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(CompactMachines3.instance, new GuiHandler());
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIds.MACHINE_VIEW.ordinal() || ID == GuiIds.MACHINE_ADMIN.ordinal()) {
            return new GuiMachineContainer(world, new BlockPos(x, y, z), player);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIds.PSD_GUIDE.ordinal()) {
            return new GuiPSDScreen();
        } else if (ID == GuiIds.MACHINE_VIEW.ordinal()) {
            return new GuiMachine(new GuiMachineContainer(world, new BlockPos(x, y, z), player), false);
        } else if (ID == GuiIds.MACHINE_ADMIN.ordinal()) {
            return new GuiMachine(new GuiMachineContainer(world, new BlockPos(x, y, z), player), true);
        }

        return null;
    }
}
