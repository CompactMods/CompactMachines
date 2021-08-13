package dev.compactmods.machines.client;

import dev.compactmods.machines.block.tiles.TunnelWallTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ClientTunnelHandler {
    public static void updateTunnelData(BlockPos position, ResourceLocation type) {
        ClientWorld w = Minecraft.getInstance().level;
        if(w == null)
            return;

        TileEntity tileEntity = w.getBlockEntity(position);
        if(tileEntity == null)
            return;

        TunnelWallTile tile = (TunnelWallTile) tileEntity;
        tile.setTunnelType(type);

        BlockState state = w.getBlockState(position);
        w.sendBlockUpdated(position, state, state, 1);
    }
}
