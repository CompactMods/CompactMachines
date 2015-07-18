package org.dave.CompactMachines.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MovingObjectPosition;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.thirdparty.lookingglass.CameraAnimatorPlayer;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.lwjgl.opengl.GL11;

import com.xcompwiz.lookingglass.api.IWorldViewAPI;
import com.xcompwiz.lookingglass.api.view.IWorldView;

public class RenderPersonalShrinkingDeviceLG extends RenderPersonalShrinkingDevice {
	private int coordLookingAt = -1;
	private IWorldView worldview;
	private IWorldViewAPI wvapi;

	public RenderPersonalShrinkingDeviceLG(Object wvapi) {
		this.wvapi = (IWorldViewAPI)wvapi;
	}

	@Override
	public void customRender(Tessellator tessellator, Block block, MovingObjectPosition pos) {
		if(!ConfigurationHandler.enableIntegrationLookingGlass) {
			super.customRender(tessellator, block, pos);
			return;
		}

		if(block == ModBlocks.machine) {
			TileEntityMachine teMachine = (TileEntityMachine) Minecraft.getMinecraft().thePlayer.worldObj.getTileEntity(pos.blockX, pos.blockY, pos.blockZ);
			if(coordLookingAt != teMachine.coords) {
				worldview =  wvapi.createWorldView(ConfigurationHandler.dimensionId, teMachine.getChunkCoordinates(), ConfigurationHandler.psdResolutionX, ConfigurationHandler.psdResolutionY);
				CameraAnimatorPlayer cam = new CameraAnimatorPlayer(worldview, teMachine);
				worldview.setAnimator(cam);
				worldview.grab();
				coordLookingAt = teMachine.coords;
			}
		} else if(worldview != null) {
			worldview.release();
			coordLookingAt = -1;
			worldview = null;
		}

		if(worldview != null) {
			worldview.markDirty();
		}

		if(!Minecraft.getMinecraft().thePlayer.isSneaking() && block == ModBlocks.machine && worldview != null) {
			GL11.glPushMatrix();

			GL11.glDepthMask(false);

			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GL11.glRotatef(180F, 0F, 0F, 1F);
			GL11.glTranslatef(-0.75F, -0.8125F, -0.0626F);
			GL11.glScalef(0.015F, 0.015F, 0.015F);
			tessellator.startDrawingQuads();
			int texture = worldview.getTexture();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			tessellator.addVertexWithUV(0, 0, 0,     0, 1);
			tessellator.addVertexWithUV(0, 25, 0,    0, 0);
			tessellator.addVertexWithUV(33.4, 25, 0, 1, 0);
			tessellator.addVertexWithUV(33.4, 0, 0,  1, 1);
			tessellator.draw();

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDepthMask(true);
			GL11.glPopMatrix();
		} else {
			super.customRender(tessellator, block, pos);
		}
	}
}
