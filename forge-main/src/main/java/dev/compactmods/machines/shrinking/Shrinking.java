package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Shrinking {

    public static final RegistryObject<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = Registries.ITEMS.register("personal_shrinking_device",
            () -> new PersonalShrinkingDevice(new Item.Properties()
                    .tab(CompactMachines.COMPACT_MACHINES_ITEMS)
                    .stacksTo(1)));

    public static void prepare() {

    }
}
