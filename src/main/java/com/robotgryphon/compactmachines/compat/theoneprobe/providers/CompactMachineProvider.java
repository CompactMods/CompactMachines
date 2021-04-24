package com.robotgryphon.compactmachines.compat.theoneprobe.providers;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.api.core.Tooltips;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.compat.theoneprobe.IProbeData;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.machine.CompactMachineInternalData;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import com.robotgryphon.compactmachines.util.TranslationUtil;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.Set;

public class CompactMachineProvider {

    public static void exec(IProbeData data, PlayerEntity player, World world, BlockState state) {
        ProbeMode mode = data.getMode();
        IProbeInfo info = data.getInfo();
        IProbeHitData hitData = data.getHitData();

        addProbeInfo(mode, info, player, world, state, hitData);
    }

    private static void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World world, BlockState state, IProbeHitData hitData) {
        TileEntity te = world.getBlockEntity(hitData.getPos());
        if (te instanceof CompactMachineTile) {
            CompactMachineTile machine = (CompactMachineTile) te;

            Optional<CompactMachineInternalData> machineData = machine.getInternalData();
            machineData.ifPresent(md -> {

                IFormattableTextComponent id = TranslationUtil
                        .tooltip(Tooltips.Machines.ID, machine.machineId)
                        .withStyle(TextFormatting.GREEN);

                info.text(id);

                // Owner Name
                PlayerEntity owner = world.getPlayerByUUID(md.getOwner());
                if (owner != null) {
                    GameProfile ownerProfile = owner.getGameProfile();
                    IFormattableTextComponent ownerText = TranslationUtil
                        .tooltip(Tooltips.Machines.OWNER, ownerProfile.getName())
                        .withStyle(TextFormatting.GRAY);

                    info.text(ownerText);
                }

                Set<BlockPos> tunnelsForMachineSide = TunnelHelper.getTunnelsForMachineSide(machine.machineId,
                        (ServerWorld) world, hitData.getSideHit());

                IProbeInfo vertical = info.vertical(info.defaultLayoutStyle().spacing(0));

                ServerWorld cm = world.getServer().getLevel(Registration.COMPACT_DIMENSION);
                tunnelsForMachineSide.forEach(pos -> {
                    TunnelWallTile tile = (TunnelWallTile) cm.getBlockEntity(pos);
                    if (tile == null)
                        return;

                    tile.getTunnelDefinition().ifPresent(tunnelDef -> {
                        vertical.text(
                                new StringTextComponent(pos.toString() + ": " + tunnelDef.getRegistryName().toString())
                        );
                    });
                });

                // TODO: Connected block info (inside)
                // TunnelHelper.getConnectedState(world, te, EnumTunnelSide.INSIDE);
            });
        }
    }
}
