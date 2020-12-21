package com.robotgryphon.compactmachines.compat.theoneprobe.providers;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.compat.theoneprobe.IProbeData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.Set;

public class CompactMachineProbeInfoProvider {

    public static void exec(IProbeData data, PlayerEntity player, World world, BlockState state) {
        ProbeMode mode = data.getMode();
        IProbeInfo info = data.getInfo();
        IProbeHitData hitData = data.getHitData();

        addProbeInfo(mode, info, player, world, state, hitData);
    }

    private static void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World world, BlockState state, IProbeHitData hitData) {
        TileEntity te = world.getTileEntity(hitData.getPos());
        if (te instanceof CompactMachineTile) {
            CompactMachineTile machine = (CompactMachineTile) te;

            Optional<CompactMachineRegistrationData> machineData = machine.getMachineData();
            machineData.ifPresent(md -> {

                IFormattableTextComponent id = new TranslationTextComponent(
                        String.format("tooltip.%s.machine_id", CompactMachines.MODID),
                        md.getId()
                ).mergeStyle(TextFormatting.GREEN);
                info.text(id);

                // Owner Name
                PlayerEntity owner = world.getPlayerByUuid(md.getOwner());
                if (owner != null) {
                    GameProfile ownerProfile = owner.getGameProfile();
                    IFormattableTextComponent ownerText = new TranslationTextComponent(
                            String.format("tooltip.%s.owner", CompactMachines.MODID), ownerProfile.getName()
                    ).mergeStyle(TextFormatting.GRAY);

                    info.text(ownerText);
                }

                Set<BlockPos> tunnelsForMachineSide = TunnelHelper.getTunnelsForMachineSide(md.getId(), (ServerWorld) world, hitData.getSideHit());
                IProbeInfo vertical = info.vertical(info.defaultLayoutStyle().spacing(0));

                tunnelsForMachineSide.forEach(pos -> {
                    vertical.text(new StringTextComponent(pos.toString()));
                });

                // TODO: Connected block info (inside)
                // TunnelHelper.getConnectedState(world, te, EnumTunnelSide.INSIDE);
            });
        }
    }
}
