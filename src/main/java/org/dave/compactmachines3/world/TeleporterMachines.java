package org.dave.compactmachines3.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TeleporterMachines extends Teleporter {

    public TeleporterMachines(WorldServer ws) {
        super(ws);
    }


    @Override
    public void placeInPortal(Entity entityIn, float rotationYaw) {
    }
}
