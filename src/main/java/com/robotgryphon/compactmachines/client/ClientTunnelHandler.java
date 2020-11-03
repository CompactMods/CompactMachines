package com.robotgryphon.compactmachines.client;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ClientTunnelHandler {
    public static void updateTunnelData(BlockPos position, ResourceLocation type) {
        ClientWorld w = Minecraft.getInstance().world;
        if(w == null)
            return;

        TileEntity tileEntity = w.getTileEntity(position);
        if(tileEntity == null)
            return;

        TunnelWallTile tile = (TunnelWallTile) tileEntity;
        tile.setTunnelType(type);

        BlockState state = w.getBlockState(position);
        w.notifyBlockUpdate(position, state, state, 1);
    }
}
