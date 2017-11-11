package org.dave.compactmachines3.world.tools;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

import java.util.List;

public class SpawnTools {
    public static int spawnEntitiesInMachine(int coords) {
        int count = 0;

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();

        EnumMachineSize size = WorldSavedDataMachines.INSTANCE.machineSizes.get(coords);
        if(size == null) {
            return count;
        }

        BlockPos start = new BlockPos((coords << 10) + 1, 41, 1);
        BlockPos end = new BlockPos((coords << 10) + size.getDimension(), 41 + size.getDimension(), 1 + size.getDimension());

        List<EntityLivingBase> livingEntities = machineWorld.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(start,end));
        int maxEntities = size.getDimension() / 2;
        if(livingEntities.size() >= maxEntities) {
            return count;
        }

        boolean playerInside = livingEntities.stream().anyMatch(entity -> entity instanceof EntityPlayer);
        for(EnumCreatureType type : EnumCreatureType.values()) {
            if(!type.getPeacefulCreature() && playerInside) {
                continue;
            }

            if(type.getPeacefulCreature() && !ConfigurationHandler.MachineSettings.allowPeacefulSpawns) {
                continue;
            }

            if(!type.getPeacefulCreature() && !ConfigurationHandler.MachineSettings.allowHostileSpawns) {
                continue;
            }

            BlockPos randomPos = start.add(machineWorld.rand.nextInt(size.getDimension()-1), machineWorld.rand.nextInt(size.getDimension()-1), machineWorld.rand.nextInt(size.getDimension()-1));
            while(randomPos.getY() > 0 && machineWorld.isAirBlock(randomPos.down())) {
                randomPos = randomPos.down();
            }
            Biome.SpawnListEntry spawnEntry = machineWorld.getSpawnListEntryForTypeAt(type, randomPos);
            if(spawnEntry == null) {
                continue;
            }

            boolean canSpawnAtPos = WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(spawnEntry.entityClass), machineWorld, randomPos);
            if(!canSpawnAtPos) {
                continue;
            }

            EntityLiving entity;
            try {
                entity = spawnEntry.newInstance(machineWorld);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if(entity == null) {
                continue;
            }

            float x = (float)randomPos.getX() + 0.5f;
            float y = (float)randomPos.getY() + 0.2f;
            float z = (float)randomPos.getZ() + 0.5f;

            entity.setLocationAndAngles((double)x, (double)y, (double)z, machineWorld.rand.nextFloat() * 360.0F, 0.0F);
            net.minecraftforge.fml.common.eventhandler.Event.Result canSpawn = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(entity, machineWorld, x, y, z, false);

            if (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW || (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT && (entity.getCanSpawnHere() && entity.isNotColliding()))) {
                if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entity, machineWorld, x, y, z)) {
                    entity.onInitialSpawn(machineWorld.getDifficultyForLocation(new BlockPos(entity)), null);
                }

                if (entity.isNotColliding()) {
                    count++;
                    machineWorld.spawnEntity(entity);
                } else {
                    entity.setDead();
                }
            }
        }

        return count;
    }
}
