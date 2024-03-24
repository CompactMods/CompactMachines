package dev.compactmods.machines.neoforge.compat.jade;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.Tooltips;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CompactMachineJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final CompactMachineJadeProvider INSTANCE = new CompactMachineJadeProvider();

    @Override
    public ResourceLocation getUid() {
        return CompactMachines.rl("bound_machine");
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor) {
        final var player = blockAccessor.getPlayer();
        if (blockAccessor.getBlockEntity() instanceof BoundCompactMachineBlockEntity machine) {
            var owner = machine.getOwnerUUID().orElse(Util.NIL_UUID);
            tag.putUUID("owner", owner);

            RoomApi.room(machine.connectedRoom()).ifPresent(inst -> {
                tag.putString("room_code", inst.code());
            });
        }
    }


    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
        final var serverData = blockAccessor.getServerData();
        if (serverData.contains("owner")) {
            final var owner = blockAccessor.getLevel().getPlayerByUUID(serverData.getUUID("owner"));
            if (owner != null) {
                GameProfile ownerProfile = owner.getGameProfile();
                MutableComponent ownerText = TranslationUtil
                        .tooltip(Tooltips.Machines.OWNER, ownerProfile.getName())
                        .withStyle(ChatFormatting.GRAY);

                tooltip.add(ownerText);
            }
        }

        if (serverData.contains("room_code")) {
            final var connectedComponent = TranslationUtil
                    .tooltip(Tooltips.Machines.BOUND_TO, serverData.getString("room_code"))
                    .withStyle(ChatFormatting.DARK_GRAY);

            tooltip.add(connectedComponent);
        }
    }
}