package dev.compactmods.machines.client.keybinds.room;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.network.room.PlayerRequestedUpgradeUIPacket;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

public class RoomUpgradeUIMapping {

   public static final String CATEGORY = Util.makeDescriptionId("key.category", CompactMachines.modRL("general"));
   public static final String NAME = Util.makeDescriptionId("key.mapping", CompactMachines.modRL("open_upgrade_screen"));

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
	  final var player = Minecraft.getInstance().player;
	  if (player != null && level != null && level.dimension().equals(CompactDimension.LEVEL_KEY)) {
		  if(CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(level.enabledFeatures())) {
			  player.getExistingData(Rooms.DataAttachments.CURRENT_ROOM_CODE).ifPresent(currentRoom -> {
                  PacketDistributor.sendToServer(new PlayerRequestedUpgradeUIPacket(currentRoom, true));
			  });
		  } else {
			  player.displayClientMessage(Component.literal("You must enable room upgrades for this keybind!"), true);
		  }
	  }
   }
}
