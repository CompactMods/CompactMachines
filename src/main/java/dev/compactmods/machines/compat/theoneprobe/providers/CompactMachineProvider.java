//package dev.compactmods.machines.compat.theoneprobe.providers;
//
//import com.mojang.authlib.GameProfile;
//import dev.compactmods.machines.CompactMachines;
//import dev.compactmods.machines.api.core.Tooltips;
//import dev.compactmods.machines.block.tiles.CompactMachineTile;
//import dev.compactmods.machines.block.tiles.TunnelWallTile;
//import dev.compactmods.machines.compat.theoneprobe.IProbeData;
//import dev.compactmods.machines.core.Registration;
//import dev.compactmods.machines.tunnels.TunnelHelper;
//import dev.compactmods.machines.util.TranslationUtil;
//import mcjty.theoneprobe.api.IProbeHitData;
//import mcjty.theoneprobe.api.IProbeInfo;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.core.BlockPos;
//import net.minecraft.network.chat.MutableComponent;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.ChatFormatting;
//import net.minecraft.world.level.Level;
//import net.minecraft.server.level.ServerLevel;
//
//import java.util.Set;
//
//public class CompactMachineProvider {
//
//    public static void exec(IProbeData data, Level world) {
//        IProbeInfo info = data.getInfo();
//        IProbeHitData hitData = data.getHitData();
//
//        addProbeInfo(info, world, hitData);
//    }
//
//    private static void addProbeInfo(IProbeInfo info, Level world, IProbeHitData hitData) {
//        BlockEntity te = world.getBlockEntity(hitData.getPos());
//        if (te instanceof CompactMachineTile) {
//            CompactMachineTile machine = (CompactMachineTile) te;
//
//            if(machine.mapped()) {
//                MutableComponent id = TranslationUtil
//                        .tooltip(Tooltips.Machines.ID, machine.machineId)
//                        .withStyle(ChatFormatting.GREEN);
//
//                info.text(id);
//            } else {
//                MutableComponent newMachine = TranslationUtil
//                        .message(new ResourceLocation(CompactMachines.MOD_ID, "new_machine"))
//                        .withStyle(ChatFormatting.GREEN);
//
//                info.text(newMachine);
//            }
//
//            machine.getOwnerUUID().ifPresent(ownerID -> {
//                // Owner Name
//                Player owner = world.getPlayerByUUID(ownerID);
//                if (owner != null) {
//                    GameProfile ownerProfile = owner.getGameProfile();
//                    MutableComponent ownerText = TranslationUtil
//                            .tooltip(Tooltips.Machines.OWNER, ownerProfile.getName())
//                            .withStyle(ChatFormatting.GRAY);
//
//                    info.text(ownerText);
//                }
//            });
//
//            Set<BlockPos> tunnelsForMachineSide = TunnelHelper.getTunnelsForMachineSide(machine.machineId,
//                    (ServerLevel) world, hitData.getSideHit());
//
//            IProbeInfo vertical = info.vertical(info.defaultLayoutStyle().spacing(0));
//
//            ServerLevel cm = world.getServer().getLevel(Registration.COMPACT_DIMENSION);
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
//        }
//    }
//}
