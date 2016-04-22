package org.dave.CompactMachines.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.lwjgl.opengl.GL11;

public class RenderPersonalShrinkingDevice implements IItemRenderer {
	private static RenderItem	renderItem	= new RenderItem();


	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.EQUIPPED_FIRST_PERSON;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	public void customRender(Tessellator tessellator, Block block, MovingObjectPosition pos) {
		String direction = "?";
		if (pos.sideHit != -1) {
			ForgeDirection dir = ForgeDirection.getOrientation(pos.sideHit);
			if (block != ModBlocks.machine) {
				dir = dir.getOpposite();
			}
			direction = dir.toString();
		}

		// Actually draw the string on the screen of the PSD
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		GL11.glPushMatrix();
		GL11.glRotatef(180F, 0F, 0F, 1F);
		GL11.glTranslatef(-0.715F, -0.785F, -0.0626F);
		GL11.glScalef(0.015F, 0.015F, 0.015F);
		font.drawString(direction, 0, 0, ConfigurationHandler.psdDisplayColor);

		// Extra data when we have a "special" block we're looking at
		GL11.glTranslatef(0F, 8F, 0F);

		if (block == ModBlocks.interfaceblock) {
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			font.drawString("Interface", 0, 0, ConfigurationHandler.psdDisplayColor);
		} else if (block == ModBlocks.machine) {
			GL11.glScalef(0.4F, 0.4F, 0.4F);
			TileEntityMachine teMachine = (TileEntityMachine) Minecraft.getMinecraft().thePlayer.worldObj.getTileEntity(pos.blockX, pos.blockY, pos.blockZ);

			if (teMachine.hasCustomName()) {
				font.drawString(teMachine.getCustomName(), 0, 0, ConfigurationHandler.psdDisplayColor);
			} else {
				font.drawString("Machine: " + (teMachine.coords == -1 ? "NEW" : teMachine.coords), 0, 0, ConfigurationHandler.psdDisplayColor);
			}

			GL11.glTranslatef(0F, 9F, 0F);
			font.drawString("Upgraded: " + (teMachine.isUpgraded ? "yes" : "no"), 0, 0, ConfigurationHandler.psdDisplayColor);
			if (teMachine.hasIntegratedPSD) {
				GL11.glTranslatef(0F, 9F, 0F);
				font.drawString("Integrated PSD", 0, 0, ConfigurationHandler.psdDisplayColor);
			}
		}

		GL11.glPopMatrix();
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		Tessellator tessellator = Tessellator.instance;

		// Use minecrafts renderer to render the item with a thickness
		IIcon icon = item.getIconIndex();
		ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), 0.0625F);

		// Precalculate the max ray trace distance by using pythagoras in 3d
		// floorDiagonalLength = Math.sqrt(13*13 + 13*13)
		// roomDiagonalLength = Math.sqrt(13*13 + floorDiagonalLength*floorDiagonalLength)
		// -> 22.51666F
		if(Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.worldObj == null) {
			return;
		}

		MovingObjectPosition pos = Minecraft.getMinecraft().thePlayer.rayTrace(22.52F, 1.0F);
		if(pos == null) {
			return;
		}

		Block block = Minecraft.getMinecraft().thePlayer.worldObj.getBlock(pos.blockX, pos.blockY, pos.blockZ);

		// We're only interested in our blocks
		if (block != ModBlocks.innerwall && block != ModBlocks.interfaceblock && block != ModBlocks.machine) {
			return;
		}

		customRender(tessellator, block, pos);
	}
}
