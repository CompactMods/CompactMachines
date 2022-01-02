package dev.compactmods.machines.client;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;
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
            BlockEntity tile = reader.getBlockEntity(position);
            if (tile instanceof TunnelWallEntity tunnel) {
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
