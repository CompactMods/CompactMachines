package org.dave.CompactMachines.thirdparty.lookingglass;

import net.minecraftforge.client.MinecraftForgeClient;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.client.render.RenderPersonalShrinkingDeviceLG;
import org.dave.CompactMachines.init.ModItems;
import org.dave.CompactMachines.utility.LogHelper;

import com.xcompwiz.lookingglass.api.APIInstanceProvider;
import com.xcompwiz.lookingglass.api.APIUndefined;
import com.xcompwiz.lookingglass.api.APIVersionRemoved;
import com.xcompwiz.lookingglass.api.APIVersionUndefined;

public class LookingGlassIntegration {

	public static void register(APIInstanceProvider provider) {
		LogHelper.info("LookingGlass API Provider Received");

		boolean registeredRenderer = false;
		try {
			Object apiinst = provider.getAPIInstance("view-1");
			if(CompactMachines.proxy.isClient()) {
				MinecraftForgeClient.registerItemRenderer(ModItems.psd, new RenderPersonalShrinkingDeviceLG(apiinst));
				registeredRenderer = true;
			}
		} catch (APIUndefined e) {
			LogHelper.warn("view-1 api is undefined. Skipping LookingGlasss integration.");
		} catch (APIVersionUndefined e) {
			LogHelper.warn("view-1 api version is undefined. Skipping LookingGlasss integration.");
		} catch (APIVersionRemoved e) {
			LogHelper.warn("view-1 api version got removed. Skipping LookingGlasss integration.");
		}

		if(!registeredRenderer) {
			CompactMachines.proxy.registerRenderers();
		}
	}
}
