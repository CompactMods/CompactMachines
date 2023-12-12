package dev.compactmods.machines.forge.tunnel;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.RedstoneTunnel;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.room.capability.RoomCapabilities;
import dev.compactmods.machines.forge.wall.SolidWallBlock;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.ITunnelItem;
import dev.compactmods.machines.tunnel.TunnelHelper;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelMachineFilters;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelTypeFilters;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TunnelItem extends Item implements ITunnelItem {
    public TunnelItem(Properties properties) {
        super(properties);
    }

    @Deprecated(forRemoval = true, since = "5.2.0")
    public static void setTunnelType(ItemStack stack, TunnelDefinition definition) {
        CompoundTag defTag = stack.getOrCreateTagElement("definition");
        defTag.putString("id", Tunnels.getRegistryId(definition).toString());
    }

    private static void setTunnelType(ItemStack stack, ResourceKey<TunnelDefinition> definition) {
        CompoundTag defTag = stack.getOrCreateTagElement("definition");
        defTag.putString("id", definition.location().toString());
    }

    @Nonnull
    public static ItemStack createStack(ResourceKey<TunnelDefinition> definition) {
        var stack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
        setTunnelType(stack, definition);
        return stack;
    }

    @Nonnull
    @Deprecated(forRemoval = true, since = "5.2.0")
    public static ItemStack createStack(TunnelDefinition definition) {
        var stack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
        setTunnelType(stack, definition);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        try {
            String key = ITunnelItem.getDefinition(stack)
                    .map(id -> TranslationUtil.tunnelId(id.location()))
                    .orElse(TranslationUtil.tunnelId(Tunnels.UNKNOWN_KEY.location()));

            return Component.translatable(key);
        }

        catch(Exception e) {
            return Component.translatable(TranslationUtil.tunnelId(Tunnels.UNKNOWN_KEY.location()));
        }
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        try {
            ITunnelItem.getDefinition(stack).ifPresent(tunnelDef -> {
                if (Screen.hasShiftDown()) {
                    MutableComponent type = Component.translatable("tooltip." + Constants.MOD_ID + ".tunnel_type", tunnelDef.location())
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

        catch(Exception e) {
            tooltip.add(TranslationUtil.tooltip(new ResourceLocation(Constants.MOD_ID, "error_tunnel_tooltip"))
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowedIn(group)) {
            IForgeRegistry<TunnelDefinition> definitions = Tunnels.TUNNEL_DEF_REGISTRY.get();
            definitions.getKeys().forEach(def -> {
                if (def.equals(Tunnels.UNKNOWN_KEY.location()))
                    return;

                ItemStack withDef = createStack(ResourceKey.create(TunnelDefinition.REGISTRY_KEY, def));
                items.add(withDef);
            });
        }
    }



    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        final Player player = context.getPlayer();
        final BlockPos position = context.getClickedPos();
        final BlockState state = level.getBlockState(position);

        if(level instanceof ServerLevel sl && sl.dimension().equals(CompactDimension.LEVEL_KEY)) {
            if (state.getBlock() instanceof SolidWallBlock && player != null) {
                ITunnelItem.getDefinition(context.getItemInHand()).ifPresent(def -> {
                    try {
                        boolean success = setupTunnelWall(sl, position, context.getClickedFace(), player, Tunnels.getDefinition(def));
                        if (success && !player.isCreative())
                            context.getItemInHand().shrink(1);
                    } catch (Exception | MissingDimensionException e) {
                        CompactMachines.LOGGER.error(e);
                    }
                });

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.FAIL;
    }

    public static Optional<IRoomHistoryItem> getMachineBindingInfo(Player player) {
        final var history = player.getCapability(RoomCapabilities.ROOM_HISTORY);

        var mapped = history.resolve().map(hist -> {
            if (!hist.hasHistory() && player instanceof ServerPlayer sp) {
                PlayerUtil.howDidYouGetThere(sp);
                return null;
            }

            return hist.peek();
        }).orElse(null);

        return Optional.ofNullable(mapped);
    }

    private static boolean setupTunnelWall(ServerLevel compactDim, BlockPos position, Direction innerFace, Player player, TunnelDefinition def) throws Exception, MissingDimensionException {
        boolean redstone = def instanceof RedstoneTunnel;
        final var tunnelId = Tunnels.getRegistryKey(def);

        final var provider = CompactRoomProvider.instance(compactDim);
        final var roomInfo = provider.findByChunk(player.chunkPosition());
        if(roomInfo.isEmpty())
            return false;

        final var roomInstance = roomInfo.get();
        final var roomTunnels = TunnelConnectionGraph.forRoom(compactDim, roomInstance.code());

        var lastEnteredMachine = getMachineBindingInfo(player);
        if (lastEnteredMachine.isEmpty()) {
            CompactMachines.LOGGER.warn("Player does not appear to have entered room via a machine;" +
                    " history is empty. If this is an error, report it.");
            return false;
        }

        var hist = lastEnteredMachine.get();
        var placedSides = roomTunnels
                .sides(TunnelMachineFilters.all(hist.getMachine()), TunnelTypeFilters.key(tunnelId))
                .collect(Collectors.toSet());

        // all tunnels already placed for type
        if (placedSides.size() == 6) {
            player.displayClientMessage(TranslationUtil.message(Messages.NO_TUNNEL_SIDE).withStyle(ChatFormatting.DARK_RED), true);
            return false;
        }

        var newlyPlacedSide = TunnelHelper.getOrderedSides()
                .filter(s -> !placedSides.contains(s))
                .findFirst();

        if (newlyPlacedSide.isEmpty()) {
            player.displayClientMessage(TranslationUtil.message(Messages.NO_TUNNEL_SIDE).withStyle(ChatFormatting.DARK_RED), true);
            return false;
        }

        Direction first = newlyPlacedSide.get();
        var tunnelState = Tunnels.BLOCK_TUNNEL_WALL.get()
                .defaultBlockState()
                .setValue(TunnelWallBlock.TUNNEL_SIDE, innerFace)
                .setValue(TunnelWallBlock.CONNECTED_SIDE, first)
                .setValue(TunnelWallBlock.REDSTONE, redstone);

        boolean connected = roomTunnels.register(position, tunnelId, hist.getMachine(), first);
        if (!connected) {
            player.displayClientMessage(TranslationUtil.message(Messages.NO_TUNNEL_SIDE), true);
            return false;
        }

        compactDim.setBlock(position, tunnelState, Block.UPDATE_ALL);
        compactDim.getBlockEntity(position, Tunnels.TUNNEL_BLOCK_ENTITY.get()).ifPresent(twe -> {
            twe.setTunnelType(tunnelId);
            twe.setConnectedTo(hist.getMachine(), first);

//            CompactMachinesNet.CHANNEL.send(
//                    PacketDistributor.TRACKING_CHUNK.with(() -> compactDim.getChunkAt(position)),
//                    new TunnelAddedPacket(position, tunnelId));
        });

        return true;
    }
}
