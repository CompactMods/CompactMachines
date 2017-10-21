package org.dave.compactmachines3.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.StructureTools;
import org.dave.compactmachines3.world.tools.TeleportationTools;

public class PlayerEventHandler {
    @SubscribeEvent
    public static void onPlayerSpawn(PlayerEvent.PlayerRespawnEvent event) {
        if(event.player.getEntityWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        int bedCoords = WorldSavedDataMachines.INSTANCE.getBedCoords(event.player);
        boolean bedInCM = bedCoords != -1;

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
            GuiMachineData.rawData = null;
            GuiMachineData.chunk = null;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(event.player.world.isRemote) {
           return;
        }

        if(event.player.world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        if (event.player.world.getTotalWorldTime() % 20 != 0) {
            return;
        }

        // No coord history -> out of here
        int lastCoords = TeleportationTools.getLastKnownCoords(event.player);
        if(lastCoords == -1) {
            event.player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:nausea"), 200, 5, false, false));
            event.player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:wither"), 160, 1, false, false));

            // And strip the previous position data, so the player actually comes out of the machine and not where he was when he last "legitimately" entered a machine
            event.player.getEntityData().removeTag("compactmachines3-oldDimension");
            event.player.getEntityData().removeTag("compactmachines3-oldPosX");
            event.player.getEntityData().removeTag("compactmachines3-oldPosY");
            event.player.getEntityData().removeTag("compactmachines3-oldPosZ");

            TeleportationTools.teleportPlayerOutOfMachineDimension((EntityPlayerMP) event.player);
            return;
        }

        if(!ConfigurationHandler.MachineSettings.keepPlayersInside) {
            return;
        }

        int actualCoords = StructureTools.getCoordsForPos(new BlockPos(event.player.posX, event.player.posY, event.player.posZ));
        EnumMachineSize enumSize = WorldSavedDataMachines.INSTANCE.machineSizes.getOrDefault(lastCoords, null);
        if(enumSize == null) {
            // This should only happen after someone updated before this feature was implemented:
            // The size value will only be known once at least one player entered the machine.
            // Handle this gracefully by doing nothing
            return;
        }

        int size = enumSize.getDimension() + 1;
        AxisAlignedBB bb = new AxisAlignedBB(
                lastCoords << 10, 40, 0,
                (lastCoords << 10) + size, 40 + size, size
                );
        if(!bb.contains(new Vec3d(event.player.posX, event.player.posY, event.player.posZ))) {
            event.player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:nausea"), 200, 5, false, false));
            event.player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:wither"), 160, 1, false, false));
            TeleportationTools.teleportPlayerOutOfMachine((EntityPlayerMP) event.player);
            return;
        }
    }
}
