package org.dave.CompactMachines.reference;

import net.minecraft.util.ResourceLocation;

import org.dave.CompactMachines.utility.ResourceLocationHelper;

public class Textures {
	public static final class Gui
	{
		private static final String				GUI_SHEET_LOCATION	= "textures/gui/";
		public static final ResourceLocation	INTERFACE			= ResourceLocationHelper.getResourceLocation(GUI_SHEET_LOCATION + "interface.png");
		public static final ResourceLocation	MACHINE				= ResourceLocationHelper.getResourceLocation(GUI_SHEET_LOCATION + "machine.png");
	}

	public static final class Entities
	{
		private static final String				ENTITY_LOCATION	= "textures/entities/";
		public static final ResourceLocation	VILLAGER		= ResourceLocationHelper.getResourceLocation(ENTITY_LOCATION + "villager.png");
	}
}
