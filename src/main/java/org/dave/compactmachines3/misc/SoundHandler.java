package org.dave.compactmachines3.misc;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.dave.compactmachines3.CompactMachines3;

public class SoundHandler {
    public static SoundEvent crafting;

    public static void init(IForgeRegistry<SoundEvent> registry) {
        crafting = createSoundEvent("miniaturization_crafting");

        registry.register(crafting);
    }

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation loc = new ResourceLocation(CompactMachines3.MODID, name);
        SoundEvent event = new SoundEvent(loc).setRegistryName(loc);

        return event;
    }


}
