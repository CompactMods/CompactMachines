package org.dave.CompactMachines.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class CMConfigGUI extends GuiConfig {
	public CMConfigGUI(GuiScreen parent) {
		super(parent, getConfigElements(), Reference.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(ConfigurationHandler.configuration.toString()));
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();

		list.add(categoryElement("compactmachines", "General", ""));
		list.add(categoryElement("integration", "Integration", ""));

		return list;
	}

	private static IConfigElement categoryElement(String category, String name, String tooltip_key) {
        return new DummyConfigElement.DummyCategoryElement(name, tooltip_key, new ConfigElement(ConfigurationHandler.configuration.getCategory(category)).getChildElements());
    }
}
