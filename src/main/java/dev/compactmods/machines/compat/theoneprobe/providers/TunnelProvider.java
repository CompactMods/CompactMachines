//package dev.compactmods.machines.compat.theoneprobe.providers;
//
//import dev.compactmods.machines.CompactMachines;
//import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
//import dev.compactmods.machines.api.tunnels.TunnelDefinition;
//import dev.compactmods.machines.block.tiles.TunnelWallTile;
//import dev.compactmods.machines.block.walls.TunnelWallBlock;
//import dev.compactmods.machines.compat.theoneprobe.IProbeData;
//import dev.compactmods.machines.core.Registration;
//import dev.compactmods.machines.teleportation.DimensionalPosition;
//import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
//import dev.compactmods.machines.tunnels.TunnelHelper;
//import mcjty.theoneprobe.api.*;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.core.Direction;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.HitResult;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.ChatFormatting;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.world.level.Level;
//import net.minecraft.server.level.ServerLevel;
//
//import java.util.Optional;
//
//public class TunnelProvider {
//
//    public static void exec(IProbeData data, Player player, Level world, BlockState state) {
//        ProbeMode mode = data.getMode();
//        IProbeInfo info = data.getInfo();
//        IProbeHitData hitData = data.getHitData();
//
//        addProbeInfo(mode, info, player, world, state, hitData);
//    }
//
//    private static void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player playerEntity, Level world, BlockState blockState, IProbeHitData hitData) {
//        Direction side = blockState.getValue(TunnelWallBlock.CONNECTED_SIDE);
//        ILayoutStyle center = info.defaultLayoutStyle()
//                .alignment(ElementAlignment.ALIGN_CENTER);
//
//        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));
//
//        TunnelWallTile tile = (TunnelWallTile) world.getBlockEntity(hitData.getPos());
//        if (tile == null)
//            return;
//
//        Optional<IDimensionalPosition> outside = TunnelHelper.getTunnelConnectedPosition(tile, EnumTunnelSide.OUTSIDE);
//        Optional<BlockState> connected = TunnelHelper.getConnectedState(tile, EnumTunnelSide.OUTSIDE);
//
//        if (probeMode == ProbeMode.EXTENDED) {
//            Optional<TunnelDefinition> definition = tile.getTunnelDefinition();
//            if (definition.isPresent()) {
//                MutableComponent tunType = new TextComponent(definition.get().getRegistryName().toString())
//                        .withStyle(ChatFormatting.GRAY);
//
//                CompoundText type = CompoundText.create().name(tunType);
//                v.horizontal(center)
//                        .item(new ItemStack(Registration.ITEM_TUNNEL.get()))
//                        .text(type);
//
//            } else {
//                ResourceLocation defID = tile
//                        .getTunnelDefinitionId()
//                        .orElse(new ResourceLocation(CompactMachines.MOD_ID, "unknown"));
//
//                MutableComponent tunType = new TranslatableComponent(CompactMachines.MOD_ID + ".errors.unknown_tunnel_type", defID)
//                        .withStyle(ChatFormatting.GRAY);
//
//                CompoundText type = CompoundText.create().name(tunType);
//                v.horizontal(center)
//                        .item(new ItemStack(Registration.ITEM_TUNNEL.get()))
//                        .text(type);
//            }
//        }
//
//        String sideTranslated = IProbeInfo.STARTLOC
//                .concat(CompactMachines.MOD_ID + ".direction.")
//                .concat(side.getName())
//                .concat(IProbeInfo.ENDLOC);
//
//        v
//                .horizontal(center)
//                .item(new ItemStack(Items.COMPASS))
//                .text(new TranslatableComponent(CompactMachines.MOD_ID + ".direction.side", sideTranslated));
//
//        connected.ifPresent(state -> {
//            if (!outside.isPresent())
//                return;
//
//            IDimensionalPosition outPos = outside.get();
//            ServerLevel connectedWorld = (ServerLevel) world;
//            BlockPos outPosBlock = outPos.getBlockPosition();
//
//            try {
//                // If connected block isn't air, show a connected block line
//                if (!state.isAir(connectedWorld, outPosBlock)) {
//                    String blockName = IProbeInfo.STARTLOC + state.getBlock().getDescriptionId() + IProbeInfo.ENDLOC;
//                    HitResult trace = new BlockHitResult(
//                            hitData.getHitVec(), hitData.getSideHit(),
//                            outPosBlock, false);
//
//                    ItemStack pick = state
//                            .getBlock()
//                            .getPickBlock(state, trace, connectedWorld, outPosBlock, playerEntity);
//
//                    v
//                            .horizontal(center)
//                            .item(pick)
//                            .text(new TranslatableComponent(CompactMachines.MOD_ID.concat(".connected_block"), blockName));
//                }
//            } catch (Exception ex) {
//                // no-op: we don't want to spam the log here
//            }
//        });
//    }
//}
