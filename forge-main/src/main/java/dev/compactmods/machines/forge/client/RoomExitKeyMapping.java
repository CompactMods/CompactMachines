package dev.compactmods.machines.forge.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.forge.network.CompactMachinesNet;
import dev.compactmods.machines.forge.network.PlayerRequestedLeavePacket;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.settings.IKeyConflictContext;

public class RoomExitKeyMapping {

    public static final String CATEGORY = Util.makeDescriptionId("key.category", new ResourceLocation(Constants.MOD_ID, "general"));
    public static final String NAME = Util.makeDescriptionId("key.mapping", new ResourceLocation(Constants.MOD_ID, "exit_room"));

    public static final IKeyConflictContext CONFLICT_CONTEXT = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            final var level = Minecraft.getInstance().level;
            return level != null && level.dimension().equals(CompactDimension.LEVEL_KEY);
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    };

    public static final KeyMapping MAPPING = new KeyMapping(NAME, CONFLICT_CONTEXT, InputConstants.UNKNOWN, CATEGORY);

    public static void handle() {
        final var level = Minecraft.getInstance().level;
        if(level != null && level.dimension().equals(CompactDimension.LEVEL_KEY))
            CompactMachinesNet.CHANNEL.sendToServer(new PlayerRequestedLeavePacket());
    }
}
