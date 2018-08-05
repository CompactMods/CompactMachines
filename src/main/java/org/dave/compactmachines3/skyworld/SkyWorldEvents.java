package org.dave.compactmachines3.skyworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.utility.Logz;

public class SkyWorldEvents {
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
