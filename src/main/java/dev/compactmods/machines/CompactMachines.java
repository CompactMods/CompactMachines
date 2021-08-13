package dev.compactmods.machines;

import dev.compactmods.machines.config.CommonConfig;
import dev.compactmods.machines.config.EnableVanillaRecipesConfigCondition;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registration;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactMachines.MOD_ID)
@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class CompactMachines {
    public static final String MOD_ID = "compactmachines";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ItemGroup COMPACT_MACHINES_ITEMS = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.MACHINE_BLOCK_ITEM_NORMAL.get());
        }
    };

    public CompactMachines() {
        // Register blocks and items
        Registration.init();

        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CraftingHelper.register(EnableVanillaRecipesConfigCondition.Serializer.INSTANCE);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.RenderTickEvent e) {
        // lol frames r good
//        ObfuscationReflectionHelper.setPrivateValue(
//                Minecraft.class,
//                Minecraft.getInstance(),
//                -100,
//                "field_71420_M"
//        );
    }
}
