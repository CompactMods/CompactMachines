package org.dave.CompactMachines.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.client.config.GuiConfig;

public class CMConfigGUI extends GuiConfig {
	public CMConfigGUI(GuiScreen parent) {
		super(parent, new ConfigElement(ConfigurationHandler.configuration.getCategory("compactmachines")).getChildElements(), Reference.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(ConfigurationHandler.configuration.toString()));
	}
}
