//package dev.compactmods.machines.compat.jade;
//
//import com.mojang.authlib.GameProfile;
//import com.mojang.serialization.Codec;
//import dev.compactmods.machines.core.CompactMachinesCore;
//import dev.compactmods.machines.CMDataAttachments;
//import dev.compactmods.machines.i18n.MachineTranslations;
//import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
//import net.minecraft.ChatFormatting;
//import net.minecraft.core.UUIDUtil;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.Identifier;
//import snownee.jade.api.BlockAccessor;
//import snownee.jade.api.IBlockComponentProvider;
//import snownee.jade.api.IServerDataProvider;
//import snownee.jade.api.ITooltip;
//import snownee.jade.api.config.IPluginConfig;
//import snownee.jade.api.ui.BoxStyle;
//import snownee.jade.api.ui.JadeUI;
//
//public class BoundMachineProviders {
//
//    public static final Identifier UID = CompactMachinesCore.identifier("bound_machine");
//
//    public static final IBlockComponentProvider COMPONENT_PROVIDER = new IBlockComponentProvider() {
//        @Override
//        public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
//            final var serverData = blockAccessor.getServerData();
//
//            serverData.getString("room_code").ifPresent(roomCode -> {
//                final var boundTxt = JadeUI.text(Component
//                        .literal(roomCode)
//                        .withStyle(ChatFormatting.DARK_GRAY));
//
//                tooltip.add(boundTxt);
//            });
//
//            if (config.get(CompactMachinesCore.identifier("show_owner"))) {
//                serverData.read("owner", UUIDUtil.CODEC).ifPresent(ownerID -> {
//
//                    final var owner = blockAccessor.getLevel().getPlayerByUUID(ownerID);
//                    if (owner != null) {
//                        GameProfile ownerProfile = owner.getGameProfile();
//
//                        final var face = new PlayerFaceElement(ownerProfile);
//
//                        var ownerName = JadeUI.text(Component
//                                .translatable(MachineTranslations.IDs.OWNER, ownerProfile.name())
//                                .withStyle(ChatFormatting.DARK_GRAY));
//
//                        var ownerTT = JadeUI.tooltip();
//                        ownerTT.add(face);
//                        ownerTT.append(JadeUI.spacer(2, 0));
//                        ownerTT.append(ownerName);
//
//                        var box = JadeUI.box(ownerTT, BoxStyle.transparent());
//                        tooltip.add(box);
//                    }
//                });
//            }
//        }
//
//        @Override
//        public Identifier getUid() {
//            return UID;
//        }
//    };
//
//    public static final IServerDataProvider<BlockAccessor> SERVER_DATA = new IServerDataProvider<>() {
//        @Override
//        public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor) {
//            if (blockAccessor.getBlockEntity() instanceof BoundCompactMachineBlockEntity machine) {
//                CompactMachinesCore.room(machine.connectedRoom()).ifPresent(inst -> {
//                    tag.store("room_code", Codec.STRING, inst.roomCode());
//                    inst.getExistingData(CMDataAttachments.ROOM_OWNER).ifPresent(owner -> {
//                        tag.store("owner", UUIDUtil.CODEC, owner);
//                    });
//                });
//            }
//        }
//
//        @Override
//        public Identifier getUid() {
//            return UID;
//        }
//    };
//}