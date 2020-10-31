package com.robotgryphon.compactmachines.block.tiles;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.util.CompactMachineUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class TunnelWallTile extends TileEntity {

    public TunnelWallTile() {
        super(Registrations.TUNNEL_WALL_TILE.get());
    }

    public Optional<CompactMachineRegistrationData> getMachineInfo() {
        if(this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            return CompactMachineUtil.getMachineInfoByInternalPosition(serverWorld, this.pos);
        }

        return Optional.empty();
    }
}
