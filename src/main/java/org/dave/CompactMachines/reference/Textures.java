package org.dave.CompactMachines.reference;

import org.dave.CompactMachines.utility.ResourceLocationHelper;

import net.minecraft.util.ResourceLocation;

public class Textures {
	public static final class Gui
    {
        private static final String GUI_SHEET_LOCATION = "textures/gui/";
        public static final ResourceLocation INTERFACE = ResourceLocationHelper.getResourceLocation(GUI_SHEET_LOCATION + "interface.png");
        public static final ResourceLocation MACHINE = ResourceLocationHelper.getResourceLocation(GUI_SHEET_LOCATION + "machine.png");
    }
}
