package org.dave.CompactMachines.client.gui.inventory;

import org.dave.CompactMachines.inventory.ContainerMachine;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Textures;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

@SideOnly(Side.CLIENT)
public class GuiMachine extends GuiContainer {
	private TileEntityMachine tileEntityMachine;

	private static int tankHeight = 16;	
	private static int[] xPositions = new int[]{ 80, 80, 80, 109, 109, 51 };
	private static int[] yPositions = new int[]{ 66, 24, 45, 66, 45, 45 };
	
	public GuiMachine(InventoryPlayer inventoryPlayer, TileEntityMachine tileEntityMachine) {
		super(new ContainerMachine(inventoryPlayer, tileEntityMachine));
		this.tileEntityMachine = tileEntityMachine;
		xSize = 176;
		ySize = 187;
	}

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        String containerName = StatCollector.translateToLocal(tileEntityMachine.getInventoryName());
        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal(Names.Containers.VANILLA_INVENTORY), 8, ySize - 96 + 2, 4210752);
        
        for (int i = 0; i < tileEntityMachine._fluidid.length; i++) {        	
			int fluidId = tileEntityMachine._fluidid[i];
			int fluidAmount = tileEntityMachine._fluidamount[i];
			int energyAmount = tileEntityMachine._energy[i];
			
			FluidStack fluid = new FluidStack(fluidId, fluidAmount);
			int tankSize = fluidAmount * tankHeight / 1000;
			int energySize = energyAmount * tankHeight / 10000;
					
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTank(xPositions[i]-4, yPositions[i]+16, fluid, tankSize);
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawEnergy(xPositions[i]+16, yPositions[i]+16, energySize);			
		}        
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(Textures.Gui.MACHINE);

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        this.drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
    }

    protected void drawEnergy(int xOffset, int yOffset, int level)
    {
		int vertOffset = 0;
				
		while (level > 0)
		{
			int texHeight = 0;
			
			if (level > 4)
			{
				texHeight = 4;
				level -= 4;
			}
			else
			{
				texHeight = level;
				level = 0;
			}

			bindTexture(Textures.Gui.MACHINE);
			this.drawTexturedModalRect(xOffset, yOffset - texHeight - vertOffset, 176, 0, 4, texHeight);
			vertOffset = vertOffset + 4;
		}
    }    
    
	protected void drawTank(int xOffset, int yOffset, FluidStack stack, int level)
	{
		if (stack == null) return;
		Fluid fluid = stack.getFluid();
		if (fluid == null) return;
		
		IIcon icon = fluid.getIcon(stack);
		if (icon == null)
			icon = Blocks.flowing_lava.getIcon(0, 0);
		
		int vertOffset = 0;
		
		while (level > 0)
		{
			int texHeight = 0;
			
			if (level > 4)
			{
				texHeight = 4;
				level -= 4;
			}
			else
			{
				texHeight = level;
				level = 0;
			}

			bindTexture(fluid);
					
			drawTexturedModelRectFromIcon(xOffset, yOffset - texHeight - vertOffset, icon, 4, texHeight);
			vertOffset = vertOffset + 4;
		}
	}    
    
	protected void bindTexture(ResourceLocation tex)
	{
		this.mc.renderEngine.bindTexture(tex);
	}
	
	protected void bindTexture(Fluid fluid)
	{
		if (fluid.getSpriteNumber() == 0)
			this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		else
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, fluid.getSpriteNumber());
	}	    
    
}
