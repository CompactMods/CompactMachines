//package dev.compactmods.machines.compat.jade;
//
//import com.mojang.authlib.GameProfile;
//import com.mojang.serialization.Codec;
//import dev.compactmods.machines.api.CompactMachines;
//import dev.compactmods.machines.api.attachment.CMDataAttachments;
//import dev.compactmods.machines.i18n.MachineTranslations;
//import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
//import dev.compactmods.machines.room.Rooms;
//import net.minecraft.ChatFormatting;
//import net.minecraft.core.UUIDUtil;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.Mth;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.phys.Vec2;
//import snownee.jade.api.BlockAccessor;
//import snownee.jade.api.IBlockComponentProvider;
//import snownee.jade.api.IServerDataProvider;
//import snownee.jade.api.ITooltip;
//import snownee.jade.api.config.IPluginConfig;
//import snownee.jade.api.ui.BoxStyle;
//import snownee.jade.api.ui.IBoxElement;
//import snownee.jade.api.ui.IElement;
//import snownee.jade.api.ui.IElementHelper;
//import snownee.jade.api.ui.ScreenDirection;
//import snownee.jade.impl.ui.ElementHelper;
//
//public class BoundMachineProviders {
//
//    public static final ResourceLocation UID = CompactMachines.modRL("bound_machine");
//
//    public static final IBlockComponentProvider COMPONENT_PROVIDER = new IBlockComponentProvider() {
//        @Override
//        public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
//            final var serverData = blockAccessor.getServerData();
//
//            final var el = IElementHelper.get();
//
//            if (serverData.contains("room_code")) {
//                final var boundTxt = el.text(Component
//                        .literal(serverData.getString("room_code"))
//                        .withStyle(ChatFormatting.DARK_GRAY));
//
//                tooltip.add(boundTxt);
//            }
//
//            if (config.get(CompactMachines.modRL("show_owner")) && serverData.contains("owner")) {
//                final var owner = blockAccessor.getLevel().getPlayerByUUID(serverData.getUUID("owner"));
//                if (owner != null) {
//                    GameProfile ownerProfile = owner.getGameProfile();
//
//                    final var face = new PlayerFaceElement(ownerProfile)
//                            .size(new Vec2(12, 12))
//                            .message(null);
//
//                    var ownerName = el.text(Component
//                            .translatable(MachineTranslations.IDs.OWNER, ownerProfile.getName())
//                            .withStyle(ChatFormatting.DARK_GRAY));
//
//                    int sizeDiffY = Mth.floor(face.getSize().y - ownerName.getSize().y) / 2;
//                    ownerName.translate(new Vec2(0, sizeDiffY));
//
//                    var ownerTT = el.tooltip();
//                    ownerTT.add(face);
//                    ownerTT.append(el.spacer(4, 0));
//                    ownerTT.append(ownerName);
//
//                    var box = el.box(ownerTT, BoxStyle.getTransparent());
//                    tooltip.add(box);
//                }
//            }
//        }
//
//        @Override
//        public ResourceLocation getUid() {
//            return UID;
//        }
//    };
//
//    public static final IServerDataProvider<BlockAccessor> SERVER_DATA = new IServerDataProvider<>() {
//        @Override
//        public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor) {
//            final var player = blockAccessor.getPlayer();
//            if (blockAccessor.getBlockEntity() instanceof BoundCompactMachineBlockEntity machine) {
//                CompactMachines.room(machine.connectedRoom()).ifPresent(inst -> {
//                    tag.store("room_code", Codec.STRING, inst.code());
//                    inst.getExistingData(CMDataAttachments.ROOM_OWNER).ifPresent(owner -> {
//                        tag.store("owner", UUIDUtil.CODEC, owner);
//                    });
//                });
//            }
//        }
//
//        @Override
//        public ResourceLocation getUid() {
//            return UID;
//        }
//    };
//}