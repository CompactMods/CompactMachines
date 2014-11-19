package org.dave.CompactMachines.igw;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * This class is meant to be copied to your own mod which implements IGW-Mod.
 * When properly implemented by instantiating a new instance somewhere in your
 * mod
 * loading stage, this will notify the player when it doesn't have IGW in the
 * instance. It also needs to have the config option enabled to
 * notify the player. This config option will be generated in its own config
 * file.
 * 
 * @author MineMaarten https://github.com/MineMaarten/IGW-mod
 */
public class IGWSupportNotifier {
	private String				supportingMod;
	private static final String	LATEST_DL_URL	= "http://minecraft.curseforge.com/mc-mods/223815-in-game-wiki-mod/files/latest";

	/**
	 * Needs to be instantiated somewhere in your mod's loading stage.
	 */
	public IGWSupportNotifier() {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT && !Loader.isModLoaded("IGWMod")) {
			File dir = new File(".", "config");
			Configuration config = new Configuration(new File(dir, "IGWMod.cfg"));
			config.load();

			if (config.get(Configuration.CATEGORY_GENERAL, "enable_missing_notification", true, "When enabled, this will notify players when IGW-Mod is not installed even though mods add support.").getBoolean()) {
				ModContainer mc = Loader.instance().activeModContainer();
				String modid = mc.getModId();
				List<ModContainer> loadedMods = Loader.instance().getActiveModList();
				for (ModContainer container : loadedMods) {
					if (container.getModId().equals(modid)) {
						supportingMod = container.getName();
						FMLCommonHandler.instance().bus().register(this);
						ClientCommandHandler.instance.registerCommand(new CommandDownloadIGW());
						break;
					}
				}
			}
			config.save();
		}
	}

	@SubscribeEvent
	public void onPlayerJoin(TickEvent.PlayerTickEvent event) {
		if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity()) {
			event.player.addChatComponentMessage(IChatComponent.Serializer.func_150699_a("[\"" + EnumChatFormatting.GOLD + "The mod " + supportingMod + " is supporting In-Game Wiki mod. " + EnumChatFormatting.GOLD + "However, In-Game Wiki isn't installed! " + "[\"," + "{\"text\":\"Download Latest\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/igwmod_download\"}}," + "\"]\"]"));
			FMLCommonHandler.instance().bus().unregister(this);
		}
	}

	private class CommandDownloadIGW extends CommandBase {

		@Override
		public int getRequiredPermissionLevel() {
			return -100;
		}

		@Override
		public String getCommandName() {
			return "igwmod_download";
		}

		@Override
		public String getCommandUsage(ICommandSender p_71518_1_) {
			return getCommandName();
		}

		@Override
		public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
			new ThreadDownloadIGW();
		}

	}

	private class ThreadDownloadIGW extends Thread {

		public ThreadDownloadIGW() {
			setName("IGW-Mod Download Thread");
			start();
		}

		@Override
		public void run() {

			try {
				if (Minecraft.getMinecraft().thePlayer != null) {
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Downloading IGW-Mod..."));
				}

				URL url = new URL(LATEST_DL_URL);
				URLConnection connection = url.openConnection();
				connection.connect();
				String fileName = "IGW-Mod.jar";//connection.getHeaderField("Content-Disposition").replace("attachment; filename=", "").replace("\"", "");
				File dir = new File(".", "mods");
				File f = new File(dir, fileName);
				FileUtils.copyURLToFile(url, f);
				if (Minecraft.getMinecraft().thePlayer != null) {
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Successfully downloaded. Restart Minecraft to apply."));
				}

				Desktop.getDesktop().open(dir);

				finalize();
			} catch (Throwable e) {
				e.printStackTrace();
				if (Minecraft.getMinecraft().thePlayer != null) {
					Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to download"));
				}
				try {
					finalize();
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
			}
		}

	}
}
