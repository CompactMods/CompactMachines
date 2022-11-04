package dev.compactmods.machines.shrinking;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class Shrinking {

    public static final Holder<Item> psd = Holder.Reference.createStandAlone(Registry.ITEM, ResourceKey.create(
            Registry.ITEM_REGISTRY, new ResourceLocation(Constants.MOD_ID, "personal_shrinking_device")
    ));

    public static final RegistryObject<PersonalShrinkingDevice> PERSONAL_SHRINKING_DEVICE = Registries.ITEMS.register("personal_shrinking_device",
            () -> new PersonalShrinkingDevice(new Item.Properties()
                    .tab(CompactMachines.COMPACT_MACHINES_ITEMS)
                    .stacksTo(1)));

    public static void prepare() {

    }
}
