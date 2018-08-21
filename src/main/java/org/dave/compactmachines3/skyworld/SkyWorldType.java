package org.dave.compactmachines3.skyworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SkyWorldType extends WorldType {
    public SkyWorldType() {
        super("compactsky");
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        int dimId = world.provider.getDimension();

        if(dimId == 0) {
            return new SkyChunkGenerator(world, generatorOptions);
        }

        return super.getChunkGenerator(world, generatorOptions);
    }

    @Override
    public int getSpawnFuzz(WorldServer world, MinecraftServer server) {
        if(world.provider.getDimension() == 0) {
            return 0;
        }

        return super.getSpawnFuzz(world, server);
    }

    @Override
    public boolean isCustomizable() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        mc.displayGuiScreen(new GuiSkyWorldConfiguration(guiCreateWorld));
    }
}
