package org.dave.compactmachines3.skyworld;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.utility.ShrinkingDeviceUtils;

public class SkyWorldEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        World world = event.player.world;
        if(world.isRemote || !(world instanceof WorldServer)) {
            return;
        }

        if(event.player.isSpectator()) {
            return;
        }

        WorldServer worldServer = (WorldServer)world;
        if(!(worldServer.getChunkProvider().chunkGenerator instanceof SkyChunkGenerator)) {
            return;
        }

        if(!event.player.isCreative() && (event.player.posY > 49.0f || event.player.posY < 39.5f)) {
            BlockPos spawnPoint = worldServer.getSpawnPoint();
            event.player.setPositionAndUpdate(spawnPoint.getX() + 0.5d, spawnPoint.getY() + 0.2d, spawnPoint.getZ() + 0.5d);
            return;
        }

        if(!ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(event.player) && !SkyWorldSavedData.instance.hasReceivedStartingInventory(event.player)) {
            ItemStack psdStack = new ItemStack(Itemss.psd, 1, 0);
            if(!event.player.addItemStackToInventory(psdStack)) {
                EntityItem entityItem = new EntityItem(world, event.player.posX, event.player.posY + event.player.getEyeHeight() + 0.5f, event.player.posZ, psdStack);
                entityItem.lifespan = 2400;
                entityItem.setPickupDelay(10);

                entityItem.motionX = 0.0f;
                entityItem.motionY = 0.15f;
                entityItem.motionZ = 0.0f;
                world.spawnEntity(entityItem);
            }

            SkyWorldSavedData.instance.addToStartingInventoryReceiverSet(event.player);
        }
    }


    @SubscribeEvent
    public static void createSpawnPoint(WorldEvent.CreateSpawnPosition event) {
        World world = event.getWorld();
        if(world.isRemote || !(world instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer)world;
        if(!(worldServer.getChunkProvider().chunkGenerator instanceof SkyChunkGenerator)) {
            return;
        }

        float centerPos = 16.0f - (SkyTerrainGenerator.ROOM_DIMENSION / 2.0f);
        int heightPos = SkyTerrainGenerator.ROOM_FLOOR_HEIGHT - SkyTerrainGenerator.ROOM_DIMENSION + 2;

        Logz.info("Overriding world spawn point");
        event.getWorld().setSpawnPoint(new BlockPos(centerPos, heightPos, centerPos));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void preventPlacingInHub(BlockEvent.PlaceEvent event) {
        World world = event.getWorld();
        if(world.isRemote || !(world instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer)world;
        if(!(worldServer.getChunkProvider().chunkGenerator instanceof SkyChunkGenerator)) {
            return;
        }


        event.getPlayer().sendStatusMessage(new TextComponentTranslation("hint.compactmachines3.skyworld.no_block_placing"), true);

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void preventBreakingInHub(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        if(world.isRemote || !(world instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer)world;
        if(!(worldServer.getChunkProvider().chunkGenerator instanceof SkyChunkGenerator)) {
            return;
        }

        event.getPlayer().sendStatusMessage(new TextComponentTranslation("hint.compactmachines3.skyworld.no_block_breaking"), true);

        event.setCanceled(true);
    }
}
