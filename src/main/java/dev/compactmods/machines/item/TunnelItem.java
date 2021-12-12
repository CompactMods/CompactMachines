package dev.compactmods.machines.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneTunnel;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import dev.compactmods.machines.block.walls.SolidWallBlock;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.network.NetworkHandler;
import dev.compactmods.machines.network.TunnelAddedPacket;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class TunnelItem extends Item {
    public TunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String key = getDefinition(stack)
                .map(def -> {
                    ResourceLocation id = def.getRegistryName();
                    return "item." + id.getNamespace() + ".tunnels." + id.getPath().replace('/', '.');
                })
                .orElse("item." + CompactMachines.MOD_ID + ".tunnels.unnamed");

        return new TranslatableComponent(key);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        getDefinition(stack).ifPresent(tunnelDef -> {
            if (Screen.hasShiftDown()) {
                MutableComponent type = new TranslatableComponent("tooltip." + CompactMachines.MOD_ID + ".tunnel_type", tunnelDef.getRegistryName())
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(ChatFormatting.ITALIC);

                tooltip.add(type);
            } else {
                tooltip.add(TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .withStyle(ChatFormatting.ITALIC));
            }
        });
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            IForgeRegistry<TunnelDefinition> definitions = RegistryManager.ACTIVE.getRegistry(TunnelDefinition.class);
            definitions.getValues().forEach(def -> {
                ItemStack withDef = new ItemStack(this, 1);
                CompoundTag defTag = withDef.getOrCreateTagElement("definition");
                defTag.putString("id", def.getRegistryName().toString());

                items.add(withDef);
            });
        }
    }

    public static Optional<TunnelDefinition> getDefinition(ItemStack stack) {
        CompoundTag defTag = stack.getOrCreateTagElement("definition");
        if (defTag.isEmpty() || !defTag.contains("id"))
            return Optional.empty();

        ResourceLocation defId = new ResourceLocation(defTag.getString("id"));
        IForgeRegistry<TunnelDefinition> tunnelReg = RegistryManager.ACTIVE.getRegistry(TunnelDefinition.class);

        if (!tunnelReg.containsKey(defId))
            return Optional.empty();

        return Optional.ofNullable(tunnelReg.getValue(defId));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        final Player player = context.getPlayer();
        final BlockPos position = context.getClickedPos();
        final BlockState state = level.getBlockState(position);

        if (state.getBlock() instanceof SolidWallBlock && player != null) {
            getDefinition(context.getItemInHand()).ifPresent(def -> {
                boolean redstone = def instanceof IRedstoneTunnel;

                var tunnelState = Tunnels.BLOCK_TUNNEL_WALL.get()
                        .defaultBlockState()
                        .setValue(TunnelWallBlock.TUNNEL_SIDE, context.getClickedFace())
                        .setValue(TunnelWallBlock.CONNECTED_SIDE, Direction.UP)
                        .setValue(TunnelWallBlock.REDSTONE, redstone);

                level.setBlock(position, tunnelState, Block.UPDATE_ALL);
                if (level.getBlockEntity(position) instanceof TunnelWallEntity tun)
                    tun.setTunnelType(def);

                level.getServer().submitAsync(() -> {
                    NetworkHandler.MAIN_CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(position)),
                            new TunnelAddedPacket(position, def));
                });

                if (!player.isCreative())
                    context.getItemInHand().shrink(1);
            });

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }
}
