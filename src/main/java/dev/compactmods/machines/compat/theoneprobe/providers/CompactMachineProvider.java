package dev.compactmods.machines.compat.theoneprobe.providers;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.block.BlockCompactMachine;
import dev.compactmods.machines.block.tiles.CompactMachineTile;
import dev.compactmods.machines.util.TranslationUtil;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CompactMachineProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(CompactMachines.MOD_ID, "machine");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, BlockState blockState, IProbeHitData hitData) {
        if(!(blockState.getBlock() instanceof BlockCompactMachine mach))
            return;

        BlockEntity te = level.getBlockEntity(hitData.getPos());
        if (te instanceof CompactMachineTile machine) {
            if(machine.mapped()) {
                MutableComponent id = TranslationUtil
                        .tooltip(Tooltips.Machines.ID, machine.machineId)
                        .withStyle(ChatFormatting.GREEN);

                info.text(id);
            } else {
                MutableComponent newMachine = TranslationUtil
                        .message(new ResourceLocation(CompactMachines.MOD_ID, "new_machine"))
                        .withStyle(ChatFormatting.GREEN);

                info.text(newMachine);
            }

            machine.getOwnerUUID().ifPresent(ownerID -> {
                // Owner Name
                Player owner = level.getPlayerByUUID(ownerID);
                if (owner != null) {
                    GameProfile ownerProfile = owner.getGameProfile();
                    MutableComponent ownerText = TranslationUtil
                            .tooltip(Tooltips.Machines.OWNER, ownerProfile.getName())
                            .withStyle(ChatFormatting.GRAY);

                    info.text(ownerText);
                }
            });

//            Set<BlockPos> tunnelsForMachineSide = TunnelHelper.getTunnelsForMachineSide(machine.machineId,
//                    (ServerLevel) level, hitData.getSideHit());
//
//            IProbeInfo vertical = info.vertical(info.defaultLayoutStyle().spacing(0));
//
//            ServerLevel cm = level.getServer().getLevel(Registration.COMPACT_DIMENSION);
//            tunnelsForMachineSide.forEach(position -> {
//                TunnelWallTile tile = (TunnelWallTile) cm.getBlockEntity(position);
//                if (tile == null)
//                    return;
//
//                tile.getTunnelDefinition().ifPresent(tunnelDef -> {
//                    vertical.text(
//                            new TextComponent(position + ": " + tunnelDef.getRegistryName().toString())
//                    );
//                });
//            });
        }
    }
}
