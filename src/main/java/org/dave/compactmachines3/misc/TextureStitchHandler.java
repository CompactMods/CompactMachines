package org.dave.compactmachines3.misc;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.CompactMachines3;

public class TextureStitchHandler {
    public static TextureAtlasSprite blockMarker;

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        blockMarker = event.getMap().registerSprite(new ResourceLocation(CompactMachines3.MODID, "particles/blockmarker"));
    }
}
