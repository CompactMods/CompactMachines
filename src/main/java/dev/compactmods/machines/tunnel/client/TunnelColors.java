package dev.compactmods.machines.tunnel.client;

import javax.annotation.Nullable;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TunnelColors implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter reader, @Nullable BlockPos position, int tintIndex) {
        try {
            if(reader == null || position == null)
                return TunnelDefinition.NO_INDICATOR_COLOR;

            BlockEntity tile = reader.getBlockEntity(position);
            if (tile instanceof TunnelWallEntity tunnel) {
                TunnelDefinition type = tunnel.getTunnelType();

                switch(tintIndex) {
                    case 0:
                        return type.ringColor();

                    case 1:
                        return type.indicatorColor();
                }
            }

            return TunnelDefinition.NO_INDICATOR_COLOR;
        }

        catch(Exception ex) {
            return TunnelDefinition.NO_INDICATOR_COLOR;
        }
    }
}
