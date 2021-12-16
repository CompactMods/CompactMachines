package dev.compactmods.machines.compat.theoneprobe.providers;

import java.util.Optional;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.util.TranslationUtil;
import mcjty.theoneprobe.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class TunnelProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(CompactMachines.MOD_ID, "tunnel");
    }

    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player playerEntity, Level level, BlockState queryState, IProbeHitData hitData) {
        if (!(queryState.getBlock() instanceof TunnelWallBlock tb)) {
            return;
        }

        Direction side = queryState.getValue(TunnelWallBlock.CONNECTED_SIDE);
        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);

        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));

        if (level.getBlockEntity(hitData.getPos()) instanceof TunnelWallEntity tile) {
            ITunnelConnection outside = tile.getConnection();

            if (probeMode == ProbeMode.EXTENDED) {
                Optional<TunnelDefinition> definition = tile.getTunnelDefinition();
                if (definition.isPresent()) {
                    MutableComponent tunType = new TextComponent(definition.get().getRegistryName().toString())
                            .withStyle(ChatFormatting.GRAY);

                    CompoundText type = CompoundText.create().name(tunType);
                    v.horizontal(center)
                            .item(new ItemStack(Tunnels.ITEM_TUNNEL.get()))
                            .text(type);

                } else {
                    TunnelDefinition tunnel = tile.getTunnelType();

                    MutableComponent tunType = TranslationUtil.message(Messages.UNKNOWN_TUNNEL, tunnel.getRegistryName())
                            .withStyle(ChatFormatting.GRAY);

                    CompoundText type = CompoundText.create().name(tunType);
                    v.horizontal(center)
                            .item(new ItemStack(Tunnels.ITEM_TUNNEL.get()))
                            .text(type);
                }
            }

            String sideTranslated = IProbeInfo.STARTLOC
                    .concat(CompactMachines.MOD_ID + ".direction.")
                    .concat(side.getName())
                    .concat(IProbeInfo.ENDLOC);

            v.horizontal(center)
                    .item(new ItemStack(Items.COMPASS))
                    .text(new TranslatableComponent(CompactMachines.MOD_ID + ".direction.side", sideTranslated));

            ServerLevel connectedWorld = (ServerLevel) level;
            BlockPos outPosBlock = outside.position().getBlockPosition();

            try {
                // If connected block isn't air, show a connected block line
                final BlockState state = outside.state();

                if (!state.isAir()) {
                    String blockName = IProbeInfo.STARTLOC + state.getBlock().getDescriptionId() + IProbeInfo.ENDLOC;
                    HitResult trace = new BlockHitResult(
                            hitData.getHitVec(), hitData.getSideHit(),
                            outPosBlock, false);

                    ItemStack pick = state
                            .getBlock()
                            .getCloneItemStack(state, trace, connectedWorld, outPosBlock, playerEntity);

                    v.horizontal(center)
                            .item(pick)
                            .text(new TranslatableComponent(CompactMachines.MOD_ID.concat(".connected_block"), blockName));
                }
            } catch (Exception ex) {
                // no-op: we don't want to spam the log here
            }
        }
    }
}
