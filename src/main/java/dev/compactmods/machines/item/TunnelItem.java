package dev.compactmods.machines.item;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.block.tiles.TunnelWallTile;
import dev.compactmods.machines.block.walls.SolidWallBlock;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneReaderTunnel;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class TunnelItem extends Item {
    public TunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        String key = getDefinition(stack)
                .map(def -> {
                    ResourceLocation id = def.getRegistryName();
                    return "item." + id.getNamespace() + ".tunnels." + id.getPath().replace('/', '.');
                })
                .orElse("item." + CompactMachines.MOD_ID + ".tunnels.unnamed");

        return new TranslationTextComponent(key);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        getDefinition(stack).ifPresent(tunnelDef -> {
            if (Screen.hasShiftDown()) {
                IFormattableTextComponent type = new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".tunnel_type", tunnelDef.getRegistryName())
                        .withStyle(TextFormatting.GRAY)
                        .withStyle(TextFormatting.ITALIC);

                tooltip.add(type);
            } else {
                tooltip.add(TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                        .withStyle(TextFormatting.DARK_GRAY)
                        .withStyle(TextFormatting.ITALIC));
            }
        });
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            IForgeRegistry<TunnelDefinition> definitions = GameRegistry.findRegistry(TunnelDefinition.class);
            definitions.getValues().forEach(def -> {
                ItemStack withDef = new ItemStack(this, 1);
                CompoundNBT defTag = withDef.getOrCreateTagElement("definition");
                defTag.putString("id", def.getRegistryName().toString());

                items.add(withDef);
            });
        }
    }

    public static Optional<TunnelDefinition> getDefinition(ItemStack stack) {
        CompoundNBT defTag = stack.getOrCreateTagElement("definition");
        if (defTag.isEmpty() || !defTag.contains("id"))
            return Optional.empty();

        ResourceLocation defId = new ResourceLocation(defTag.getString("id"));
        IForgeRegistry<TunnelDefinition> tunnelReg = GameRegistry.findRegistry(TunnelDefinition.class);

        if (!tunnelReg.containsKey(defId))
            return Optional.empty();

        return Optional.ofNullable(tunnelReg.getValue(defId));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        final World level = context.getLevel();
        if(!level.isClientSide) {
            final PlayerEntity player = context.getPlayer();
            final BlockState state = level.getBlockState(context.getClickedPos());

            if(state.getBlock() instanceof SolidWallBlock && player != null) {
                player.displayClientMessage(TranslationUtil.message(new ResourceLocation(CompactMachines.MOD_ID, "tunnels_nyi")), true);
            }
        }

        return ActionResultType.sidedSuccess(level.isClientSide);
    }
}
