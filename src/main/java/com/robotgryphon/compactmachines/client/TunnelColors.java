package com.robotgryphon.compactmachines.client;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

public class TunnelColors implements IBlockColor {

    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader reader, @Nullable BlockPos pos, int tintIndex) {
        try {
            TileEntity tile = reader.getTileEntity(pos);
            if (tile instanceof TunnelWallTile) {
                TunnelWallTile tunnel = (TunnelWallTile) tile;
                Optional<TunnelDefinition> tunnelDefinition = tunnel.getTunnelDefinition();

                switch(tintIndex) {
                    case 0:
                        return tunnelDefinition.map(TunnelDefinition::getTunnelRingColor)
                                .orElse(Color.gray.getRGB());
                    case 1:
                        return tunnelDefinition.map(TunnelDefinition::getTunnelIndicatorColor)
                                .orElse(TunnelDefinition.NO_INDICATOR_COLOR);
                }
            }

            return Color.gray.getRGB();
        }

        catch(Exception ex) {
            return Color.red.getRGB();
        }
    }
}
