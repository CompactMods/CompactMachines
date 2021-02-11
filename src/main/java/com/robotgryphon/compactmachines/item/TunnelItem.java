package com.robotgryphon.compactmachines.item;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.api.tunnels.redstone.IRedstoneReaderTunnel;
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
import net.minecraft.util.registry.Registry;
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
    public ITextComponent getDisplayName(ItemStack stack) {
        String key = getDefinition(stack)
                .map(def -> {
                    ResourceLocation id = def.getRegistryName();
                    return "item." + id.getNamespace() + ".tunnels." + id.getPath().replace('/', '.');
                })
                .orElse("item." + CompactMachines.MOD_ID + ".tunnels.unnamed");

        return new TranslationTextComponent(key);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        getDefinition(stack).ifPresent(tunnelDef -> {
            if (Screen.hasShiftDown()) {
                IFormattableTextComponent type = new TranslationTextComponent("tooltip." + CompactMachines.MOD_ID + ".tunnel_type", tunnelDef.getRegistryName())
                        .mergeStyle(TextFormatting.GRAY)
                        .mergeStyle(TextFormatting.ITALIC);

                tooltip.add(type);
            }
        });
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            IForgeRegistry<TunnelDefinition> definitions = GameRegistry.findRegistry(TunnelDefinition.class);
            definitions.getValues().forEach(def -> {
                ItemStack withDef = new ItemStack(this, 1);
                CompoundNBT defTag = withDef.getOrCreateChildTag("definition");
                defTag.putString("id", def.getRegistryName().toString());

                items.add(withDef);
            });
        }
    }

    public static Optional<TunnelDefinition> getDefinition(ItemStack stack) {
        CompoundNBT defTag = stack.getOrCreateChildTag("definition");
        if (defTag.isEmpty() || !defTag.contains("id"))
            return Optional.empty();

        ResourceLocation defId = new ResourceLocation(defTag.getString("id"));
        IForgeRegistry<TunnelDefinition> tunnelReg = GameRegistry.findRegistry(TunnelDefinition.class);

        if (!tunnelReg.containsKey(defId))
            return Optional.empty();

        return Optional.ofNullable(tunnelReg.getValue(defId));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World w = context.getWorld();
        if (w.isRemote())
            return ActionResultType.SUCCESS;

        BlockPos pos = context.getPos();
        BlockState blockState = w.getBlockState(pos);

        if (blockState.getBlock() != Registration.BLOCK_SOLID_WALL.get())
            return ActionResultType.FAIL;

        if (context.getPlayer() instanceof ServerPlayerEntity) {
            ItemStack is = context.getItem();
            Item i = is.getItem();

            TunnelItem ti = ((TunnelItem) i);
            Optional<TunnelDefinition> definition = ti.getDefinition(context.getItem());

            definition.ifPresent(def -> {
                BlockState tunnelState = Registration.BLOCK_TUNNEL_WALL.get()
                        .getDefaultState()
                        .with(TunnelWallBlock.TUNNEL_SIDE, context.getFace());

                // Redstone Support
                boolean redstone = (def instanceof IRedstoneReaderTunnel);
                tunnelState = tunnelState.with(TunnelWallBlock.REDSTONE, redstone);
                w.setBlockState(pos, tunnelState, 3);

                // Get the server and add a deferred task - allows the tile to be created on the client first
                MinecraftServer server = ((ServerWorld) context.getWorld()).getServer();
                server.deferTask(() -> {
                    TunnelWallTile tile = (TunnelWallTile) context.getWorld().getTileEntity(context.getPos());
                    tile.setTunnelType(def.getRegistryName());
                });

                is.shrink(1);
            });

            return ActionResultType.CONSUME;
        }

        return ActionResultType.FAIL;
    }

    /**
     * Implementation for easily swapping a tunnel type for another.
     *
     * @param type
     * @param player
     * @param hand
     * @return
     */
    protected ActionResult<ItemStack> swapTunnelType(Item type, PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            if (player.isSneaking()) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                ItemStack stack = serverPlayer.getHeldItem(hand);
                ItemStack newStack = new ItemStack(type, stack.getCount());

                serverPlayer.setHeldItem(hand, newStack);

                IFormattableTextComponent msg = new StringTextComponent("switch state")
                        .mergeStyle(TextFormatting.GOLD);

                serverPlayer.sendStatusMessage(msg, true);
                return ActionResult.resultConsume(newStack);
            }
        }

        return ActionResult.resultPass(player.getHeldItem(hand));
    }


}
