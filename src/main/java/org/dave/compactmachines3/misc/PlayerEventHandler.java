package org.dave.compactmachines3.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.gui.machine.GuiMachineChunkHolder;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.TeleportationTools;

public class PlayerEventHandler {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)event.getEntity();

        if(player.getEntityWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        //TeleportationTools.teleportPlayerOutOfMachine(player);
        Logz.info("Player %s died in Compact Machine dimension.", player.getDisplayNameString());
    }

    @SubscribeEvent
    public static void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        if(event.player.getEntityWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        int bedCoords = WorldSavedDataMachines.INSTANCE.getBedCoords(event.player);
        boolean bedInCM = bedCoords != -1;

        Logz.info("Player is allowed to spawn in CM: %s, bedcoors = %d", ConfigurationHandler.MachineSettings.allowRespawning, bedCoords);
        if(bedInCM && ConfigurationHandler.MachineSettings.allowRespawning) {
            TeleportationTools.teleportPlayerToMachine((EntityPlayerMP) event.player, bedCoords, false);
        } else {
            TeleportationTools.teleportPlayerOutOfMachineDimension((EntityPlayerMP) event.player);
            event.player.getEntityData().removeTag("compactmachines3-coordHistory");
        }
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if(event.getEntityPlayer().world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        if (!ConfigurationHandler.MachineSettings.allowRespawning) {
            event.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_HERE);
            event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("hint.compactmachines3.cant_sleep_here", new Object[0]), true);
            return;
        }

        if (event.getEntityPlayer().isPlayerSleeping() || !event.getEntityPlayer().isEntityAlive()) {
            event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
            return;
        }

        event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("hint.compactmachines3.bed_position_set", new Object[0]), true);
        WorldSavedDataMachines.INSTANCE.setBedCoords(event.getEntityPlayer());

        event.setResult(EntityPlayer.SleepResult.OK);
    }


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onGuiClose(GuiOpenEvent event) {
        if(event.getGui() == null) {
            GuiMachineChunkHolder.rawData = null;
            GuiMachineChunkHolder.chunk = null;
        }
    }
}
