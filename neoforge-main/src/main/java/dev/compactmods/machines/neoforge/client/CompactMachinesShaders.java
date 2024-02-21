package dev.compactmods.machines.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

import static dev.compactmods.machines.api.Constants.MOD_ID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompactMachinesShaders
{
    private static ShaderInstance blockFullbrightShader;
    private static ShaderInstance wallShader;

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent ev) throws IOException
    {
        ev.registerShader(
                new ShaderInstance(ev.getResourceProvider(), new ResourceLocation(MOD_ID, "block_fullbright"), DefaultVertexFormat.BLOCK),
                shader -> blockFullbrightShader = shader
        );

        ev.registerShader(
                new ShaderInstance(ev.getResourceProvider(), new ResourceLocation(MOD_ID, "wall"), DefaultVertexFormat.BLOCK),
                shader -> wallShader = shader
        );
    }

    public static ShaderInstance wall() { return wallShader; }
    public static ShaderInstance fullbright()
    {
        return blockFullbrightShader;
    }
}
