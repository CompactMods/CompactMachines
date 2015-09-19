package org.dave.CompactMachines.client.gui.inventory;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.integration.thaumcraft.ThaumcraftSharedStorage;
import org.dave.CompactMachines.inventory.ContainerMachine;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Textures;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMachine extends GuiContainer {
	private TileEntityMachine	tileEntityMachine;

	private static int			tankHeight	= 16;
	private static int[]		xPositions	= new int[] { 80, 80, 80, 109, 109, 51 };
	private static int[]		yPositions	= new int[] { 66, 24, 45, 66, 45, 45 };

	private static IIcon		essentiaIcon;

	public GuiMachine(InventoryPlayer inventoryPlayer, TileEntityMachine tileEntityMachine) {
		super(new ContainerMachine(inventoryPlayer, tileEntityMachine));
		this.tileEntityMachine = tileEntityMachine;
		xSize = 176;
		ySize = 187;
	}

	protected boolean isPointInRegion(int x, int y, int w, int h, int a, int b) {
		return func_146978_c(x, y, w, h, a, b);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);

		drawTooltips(mouseX, mouseY);
	}

	public void drawTooltips(int mouseX, int mouseY) {
		List<String> lines = new ArrayList<String>(2);

		for (int i = 0; i < tileEntityMachine._fluidid.length; i++) {
			int fluidId = tileEntityMachine._fluidid[i];
			Fluid fluid = FluidRegistry.getFluid(fluidId);
			int fluidAmount = tileEntityMachine._fluidamount[i];
			int gasId = tileEntityMachine._gasid[i];
			int gasAmount = tileEntityMachine._gasamount[i];
			int energyAmount = tileEntityMachine._energy[i];
			int manaAmount = tileEntityMachine._mana;
			int aspectId = tileEntityMachine._aspectid[i];
			int aspectAmount = tileEntityMachine._aspectamount[i];

			if (isPointInRegion(xPositions[i] - 4, yPositions[i], 24, 16, mouseX, mouseY)) {
				String side = ForgeDirection.getOrientation(i).toString();
				side = side.substring(0, 1) + side.substring(1).toLowerCase();
				lines.add(side);

				if (energyAmount > 0) {
					lines.add("RF: " + energyAmount);
				}

				if (fluidAmount > 0) {
					FluidStack fluidStack = new FluidStack(fluid, fluidAmount);
					lines.add(fluidStack.getLocalizedName() + ": " + fluidAmount + "mB");
				}

				if (gasAmount > 0) {
					GasStack gasStack = new GasStack(gasId, gasAmount);
					Gas gas = gasStack.getGas();

					lines.add(gas.getLocalizedName() + ": " + gasAmount);
				}

				if (manaAmount > 0) {
					// TODO: Get rid of hardcoded capacities
					double ratio = (manaAmount * 1.0 / ConfigurationHandler.capacityMana);
					if(ratio > 1) {
						ratio = 1.0;
					}
					lines.add(String.format("%s: %.1f%%", StatCollector.translateToLocal("tooltip.cm:machine.mana"), ratio * 100));
				}

				if (aspectAmount > 0) {
					Aspect aspect = ThaumcraftSharedStorage.getAspectForID(aspectId);
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					if (player != null && aspect != null) {
						if (ThaumcraftApiHelper.hasDiscoveredAspect(player.getCommandSenderName(), aspect)) {
							lines.add(aspect.getName() + ": " + aspectAmount);
						} else {
							lines.add(StatCollector.translateToLocal("tooltip.cm:machine.unknownaspect") + ": " + aspectAmount);
						}
					}
				}
			}
		}

		if (lines.size() > 0) {
			drawTooltip(lines, mouseX, mouseY);
		}
	}

	// Thanks Minefactory Reloaded
	protected void drawTooltip(List<String> lines, int x, int y) {
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_LIGHTING);

		int tooltipWidth = 0;
		int tempWidth;
		int xStart;
		int yStart;

		for (int i = 0; i < lines.size(); i++) {
			tempWidth = this.fontRendererObj.getStringWidth(lines.get(i));

			if (tempWidth > tooltipWidth) {
				tooltipWidth = tempWidth;
			}
		}

		xStart = x - (12 + tooltipWidth);
		yStart = y - 12;
		int tooltipHeight = 8;

		if (lines.size() > 1) {
			tooltipHeight += 2 + (lines.size() - 1) * 10;
		}

		if (this.guiTop + yStart + tooltipHeight + 6 > this.height) {
			yStart = this.height - tooltipHeight - this.guiTop - 6;
		}

		this.zLevel = 300.0F;
		itemRender.zLevel = 300.0F;
		int color1 = -267386864;
		this.drawGradientRect(xStart - 3, yStart - 4, xStart + tooltipWidth + 3, yStart - 3, color1, color1);
		this.drawGradientRect(xStart - 3, yStart + tooltipHeight + 3, xStart + tooltipWidth + 3, yStart + tooltipHeight + 4, color1, color1);
		this.drawGradientRect(xStart - 3, yStart - 3, xStart + tooltipWidth + 3, yStart + tooltipHeight + 3, color1, color1);
		this.drawGradientRect(xStart - 4, yStart - 3, xStart - 3, yStart + tooltipHeight + 3, color1, color1);
		this.drawGradientRect(xStart + tooltipWidth + 3, yStart - 3, xStart + tooltipWidth + 4, yStart + tooltipHeight + 3, color1, color1);
		int color2 = 1347420415;
		int color3 = (color2 & 16711422) >> 1 | color2 & -16777216;
		this.drawGradientRect(xStart - 3, yStart - 3 + 1, xStart - 3 + 1, yStart + tooltipHeight + 3 - 1, color2, color3);
		this.drawGradientRect(xStart + tooltipWidth + 2, yStart - 3 + 1, xStart + tooltipWidth + 3, yStart + tooltipHeight + 3 - 1, color2, color3);
		this.drawGradientRect(xStart - 3, yStart - 3, xStart + tooltipWidth + 3, yStart - 3 + 1, color2, color2);
		this.drawGradientRect(xStart - 3, yStart + tooltipHeight + 2, xStart + tooltipWidth + 3, yStart + tooltipHeight + 3, color3, color3);

		for (int stringIndex = 0; stringIndex < lines.size(); ++stringIndex) {
			String line = lines.get(stringIndex);

			if (stringIndex == 0) {
				line = "\u00a7" + Integer.toHexString(15) + line;
			} else {
				line = "\u00a77" + line;
			}

			this.fontRendererObj.drawStringWithShadow(line, xStart, yStart, -1);

			if (stringIndex == 0) {
				yStart += 2;
			}

			yStart += 10;
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		this.zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		String containerName = StatCollector.translateToLocal(tileEntityMachine.getInventoryName());
		fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
		fontRendererObj.drawString(StatCollector.translateToLocal(Names.Containers.VANILLA_INVENTORY), 8, ySize - 96 + 2, 4210752);

		for (int i = 0; i < tileEntityMachine._fluidid.length; i++) {
			int fluidId = tileEntityMachine._fluidid[i];
			int fluidAmount = tileEntityMachine._fluidamount[i];
			int gasAmount = tileEntityMachine._gasamount[i];

			Fluid fluid = FluidRegistry.getFluid(fluidId);
			if(fluid != null && fluidAmount > 0) {
				FluidStack fluidStack = new FluidStack(fluid, fluidAmount);
				int tankSize = fluidAmount * tankHeight / ConfigurationHandler.capacityFluid;

				drawFluidTank(xPositions[i] - 4, yPositions[i] + 16, fluidStack, tankSize, gasAmount > 0);
			}

			int gasId = tileEntityMachine._gasid[i];
			if (gasId != -1 && gasAmount > 0) {
				GasStack gas = new GasStack(gasId, gasAmount);
				int tankSize = gasAmount * tankHeight / ConfigurationHandler.capacityGas;
				int xOffsetDelta = fluidAmount > 0 ? 2 : 4;

				drawGasTank(xPositions[i] - xOffsetDelta, yPositions[i] + 16, gas, tankSize, fluidAmount > 0);
			}

			int energyAmount = tileEntityMachine._energy[i];
			int aspectAmount = tileEntityMachine._aspectamount[i];
			if(energyAmount > 0) {
				int energySize = energyAmount * tankHeight / ConfigurationHandler.capacityRF;
				drawEnergyTank(xPositions[i] + 16, yPositions[i] + 16, Textures.Gui.INTERFACE, 176, 0, energySize, aspectAmount > 0);
			}

			if (aspectAmount > 0) {
				int tankSize = aspectAmount * tankHeight / ConfigurationHandler.capacityEssentia;
				Aspect aspect = ThaumcraftSharedStorage.getAspectForID(tileEntityMachine._aspectid[i]);
				int xOffsetDelta = energyAmount > 0 ? 2 : 0;	
				drawEssentiaTank(xPositions[i] + 16 + xOffsetDelta, yPositions[i] + 16, aspect, tankSize, energyAmount > 0);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		this.mc.getTextureManager().bindTexture(Textures.Gui.MACHINE);

		int xStart = (width - xSize) / 2;
		int yStart = (height - ySize) / 2;
		this.drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
	}

	protected void drawGauge(int xOffset, int yOffset, IIcon icon, ResourceLocation texSheet, int level, boolean halfWidth) {
		int vertOffset = 0;

		if (texSheet != null) {
			bindTexture(texSheet);
		}

		while (level > 0) {
			int texHeight = 0;

			if (level > 4) {
				texHeight = 4;
				level -= 4;
			} else {
				texHeight = level;
				level = 0;
			}

			drawTexturedModelRectFromIcon(xOffset, yOffset - texHeight - vertOffset, icon, halfWidth ? 2 : 4, texHeight);
			vertOffset = vertOffset + 4;
		}
	}

	protected void drawEnergyTank(int xOffset, int yOffset, ResourceLocation tex, int u, int v, int level, boolean halfWidth) {
		int vertOffset = 0;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(tex);

		while (level > 0) {
			int texHeight = 0;

			if (level > 4) {
				texHeight = 4;
				level -= 4;
			} else {
				texHeight = level;
				level = 0;
			}

			this.drawTexturedModalRect(xOffset, yOffset - texHeight - vertOffset, u, v, halfWidth ? 2 : 4, texHeight);
			vertOffset = vertOffset + 4;
		}
	}

	protected void drawGasTank(int xOffset, int yOffset, GasStack stack, int level, boolean halfWidth) {
		if (stack == null) {
			return;
		}
		Gas gas = stack.getGas();
		if (gas == null) {
			return;
		}

		IIcon icon = gas.getIcon();
		if (icon == null) {
			// TODO: Proper fallback?
			icon = Blocks.flowing_lava.getIcon(0, 0);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawGauge(xOffset, yOffset, icon, TextureMap.locationBlocksTexture, level, halfWidth);
	}

	protected void drawFluidTank(int xOffset, int yOffset, FluidStack fluidStack, int level, boolean halfWidth) {
		if (fluidStack == null) {
			return;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid == null) {
			return;
		}

		IIcon icon = fluid.getIcon(fluidStack);
		if (icon == null) {
			icon = Blocks.flowing_lava.getIcon(0, 0);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(fluid);
		drawGauge(xOffset, yOffset, icon, null, level, halfWidth);
	}

	protected void drawEssentiaTank(int xOffset, int yOffset, Aspect aspect, int level, boolean halfWidth) {
		if(essentiaIcon == null) {
			try {
				Block block = Block.getBlockFromItem(ItemApi.getBlock("blockJar", 0).getItem());
				Field iconFld;
				iconFld = block.getClass().getField("iconLiquid");
				essentiaIcon = (IIcon) iconFld.get(block);
			} catch (Exception e) {
				essentiaIcon = Blocks.flowing_lava.getIcon(0, 0);
			}
		}

		Color aspectColor =  new Color(aspect.getColor());
		GL11.glColor4f(aspectColor.getRed() / 256f, aspectColor.getGreen() / 256f, aspectColor.getBlue() / 256f, aspectColor.getAlpha() / 256f);

		drawGauge(xOffset, yOffset, essentiaIcon, TextureMap.locationBlocksTexture, level, halfWidth);
	}

	protected void bindTexture(ResourceLocation tex) {
		this.mc.renderEngine.bindTexture(tex);
	}

	protected void bindTexture(Fluid fluid) {
		if (fluid.getSpriteNumber() == 0) {
			this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		} else {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, fluid.getSpriteNumber());
		}
	}

}
