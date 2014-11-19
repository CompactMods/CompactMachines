package org.dave.CompactMachines.igw;

import igwmod.gui.GuiWiki;
import igwmod.gui.tabs.BaseWikiTab;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import org.dave.CompactMachines.init.ModItems;
import org.dave.CompactMachines.reference.Reference;

public class IGWTab extends BaseWikiTab {

	public IGWTab() {
		pageEntries.add("basics");
		skipLine();
		pageEntries.add("block.machine");
		pageEntries.add("block.interface");
		skipLine();
		pageEntries.add("item.psd");
		pageEntries.add("item.quantumentangler");
	}

	@Override
	public String getName() {
		return "itemGroup." + Reference.MOD_ID.toLowerCase();
	}

	@Override
	public ItemStack renderTabIcon(GuiWiki gui) {
		return new ItemStack(ModItems.personalShrinkingDevice);
	}

	@Override
	protected String getPageName(String pageEntry) {
		if (pageEntry.startsWith("block")) {
			return I18n.format("tile." + Reference.MOD_ID.toLowerCase() + ":" + pageEntry.substring(6) + ".name");
		} else if (pageEntry.startsWith("item")) {
			return I18n.format("item." + Reference.MOD_ID.toLowerCase() + ":" + pageEntry.substring(5) + ".name");
		} else {
			return I18n.format("igwpage." + pageEntry);
		}

	}

	@Override
	protected String getPageLocation(String pageEntry) {
		return "compactmachines/" + pageEntry;
	}

}
