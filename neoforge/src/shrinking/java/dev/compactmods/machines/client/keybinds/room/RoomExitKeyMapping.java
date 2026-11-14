package dev.compactmods.machines.client.keybinds.room;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.room.client.RoomKeyMappings;
import dev.compactmods.machines.network.room.PlayerRequestedLeavePacket;
import net.minecraft.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public class RoomExitKeyMapping {

   public interface I18n {
	  String NAME = Util.makeDescriptionId("key.mapping", CompactMachinesCore.identifier("exit_room"));
   }

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

   public static final KeyMapping MAPPING = new KeyMapping(I18n.NAME, CONFLICT_CONTEXT, InputConstants.UNKNOWN, RoomKeyMappings.CATEGORY);

   public static void handle() {
	  final var level = Minecraft.getInstance().level;
	  if (level != null && level.dimension().equals(CompactDimension.LEVEL_KEY))
		 ClientPacketDistributor.sendToServer(new PlayerRequestedLeavePacket());
   }
}
