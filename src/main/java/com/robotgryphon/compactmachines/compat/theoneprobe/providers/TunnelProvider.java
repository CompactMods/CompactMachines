package com.robotgryphon.compactmachines.compat.theoneprobe.providers;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.compat.theoneprobe.IProbeData;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.api.tunnels.EnumTunnelSide;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class TunnelProvider {

    public static void exec(IProbeData data, PlayerEntity player, World world, BlockState state) {
        ProbeMode mode = data.getMode();
        IProbeInfo info = data.getInfo();
        IProbeHitData hitData = data.getHitData();

        addProbeInfo(mode, info, player, world, state, hitData);
    }

    private static void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData hitData) {
        Direction side = blockState.getValue(TunnelWallBlock.CONNECTED_SIDE);
        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);

        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));

        TunnelWallTile tile = (TunnelWallTile) world.getBlockEntity(hitData.getPos());
        if (tile == null)
            return;

        Optional<DimensionalPosition> outside = TunnelHelper.getTunnelConnectedPosition(tile, EnumTunnelSide.OUTSIDE);
        Optional<BlockState> connected = TunnelHelper.getConnectedState(tile, EnumTunnelSide.OUTSIDE);

        if (probeMode == ProbeMode.EXTENDED) {
            Optional<TunnelDefinition> definition = tile.getTunnelDefinition();
            if (definition.isPresent()) {
                IFormattableTextComponent tunType = new StringTextComponent(definition.get().getRegistryName().toString())
                        .withStyle(TextFormatting.GRAY);

                CompoundText type = CompoundText.create().name(tunType);
                v.horizontal(center)
                        .item(new ItemStack(Registration.ITEM_TUNNEL.get()))
                        .text(type);

            } else {
                ResourceLocation defID = tile
                        .getTunnelDefinitionId()
                        .orElse(new ResourceLocation(CompactMachines.MOD_ID, "unknown"));

                IFormattableTextComponent tunType = new TranslationTextComponent(CompactMachines.MOD_ID + ".errors.unknown_tunnel_type", defID)
                        .withStyle(TextFormatting.GRAY);

                CompoundText type = CompoundText.create().name(tunType);
                v.horizontal(center)
                        .item(new ItemStack(Registration.ITEM_TUNNEL.get()))
                        .text(type);
            }
        }

        String sideTranslated = IProbeInfo.STARTLOC
                .concat(CompactMachines.MOD_ID + ".direction.")
                .concat(side.getName())
                .concat(IProbeInfo.ENDLOC);

        v
                .horizontal(center)
                .item(new ItemStack(Items.COMPASS))
                .text(new TranslationTextComponent(CompactMachines.MOD_ID + ".direction.side", sideTranslated));

        connected.ifPresent(state -> {
            if (!outside.isPresent())
                return;

            DimensionalPosition outPos = outside.get();
            ServerWorld connectedWorld = (ServerWorld) world;
            BlockPos outPosBlock = outPos.getBlockPosition();

            try {
                // If connected block isn't air, show a connected block line
                if (!state.isAir(connectedWorld, outPosBlock)) {
                    String blockName = IProbeInfo.STARTLOC + state.getBlock().getDescriptionId() + IProbeInfo.ENDLOC;
                    RayTraceResult trace = new BlockRayTraceResult(
                            hitData.getHitVec(), hitData.getSideHit(),
                            outPosBlock, false);

                    ItemStack pick = state
                            .getBlock()
                            .getPickBlock(state, trace, connectedWorld, outPosBlock, playerEntity);

                    v
                            .horizontal(center)
                            .item(pick)
                            .text(new TranslationTextComponent(CompactMachines.MOD_ID.concat(".connected_block"), blockName));
                }
            } catch (Exception ex) {
                // no-op: we don't want to spam the log here
            }
        });
    }
}
