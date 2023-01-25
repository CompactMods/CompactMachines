package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneTunnel;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.core.CompactMachinesNet;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.network.TunnelAddedPacket;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.wall.SolidWallBlock;
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
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TunnelItem extends Item {
    public TunnelItem(Properties properties) {
        super(properties);
    }

    public static void setTunnelType(ItemStack stack, TunnelDefinition definition) {
        CompoundTag defTag = stack.getOrCreateTagElement("definition");
        defTag.putString("id", definition.getRegistryName().toString());
    }

    @Nonnull
    public static ItemStack createStack(TunnelDefinition definition) {
        var stack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
        setTunnelType(stack, definition);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        String key = getDefinition(stack)
                .map(def -> {
                    ResourceLocation id = def.getRegistryName();
                    return TranslationUtil.tunnelId(id);
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
            IForgeRegistry<TunnelDefinition> definitions = Tunnels.TUNNEL_DEF_REGISTRY.get();
            definitions.getValues().forEach(def -> {
                if (def == Tunnels.UNKNOWN.get())
                    return;

                ItemStack withDef = createStack(def);
                items.add(withDef);
            });
        }
    }

    public static Optional<TunnelDefinition> getDefinition(ItemStack stack) {
        CompoundTag defTag = stack.getOrCreateTagElement("definition");
        if (defTag.isEmpty() || !defTag.contains("id"))
            return Optional.empty();

        ResourceLocation defId = new ResourceLocation(defTag.getString("id"));
        if (!Tunnels.isRegistered(defId))
            return Optional.empty();

        TunnelDefinition tunnelReg = Tunnels.getDefinition(defId);
        return Optional.ofNullable(tunnelReg);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        final Player player = context.getPlayer();
        final BlockPos position = context.getClickedPos();
        final BlockState state = level.getBlockState(position);

        if(level instanceof ServerLevel sl && sl.dimension().equals(Registration.COMPACT_DIMENSION)) {
            if (state.getBlock() instanceof SolidWallBlock && player != null) {
                getDefinition(context.getItemInHand()).ifPresent(def -> {
                    try {
                        boolean success = setupTunnelWall(sl, position, context.getClickedFace(), player, def);
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
        final var history = player.getCapability(Capabilities.ROOM_HISTORY);

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
        boolean redstone = def instanceof IRedstoneTunnel;

        final var server = compactDim.getServer();
        final var roomTunnels = TunnelConnectionGraph.forRoom(compactDim, player.chunkPosition());

        var lastEnteredMachine = getMachineBindingInfo(player);
        if (lastEnteredMachine.isEmpty()) {
            CompactMachines.LOGGER.warn("Player does not appear to have entered room via a machine;" +
                    " history is empty. If this is an error, report it.");
            return false;
        }

        var hist = lastEnteredMachine.get();
        var placedSides = roomTunnels
                .getTunnelSides(def)
                .collect(Collectors.toSet());

        // all tunnels already placed for type
        if (placedSides.size() == 6)
            return false;

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

        boolean connected = roomTunnels.registerTunnel(position, def, hist.getMachine(), first);
        if (!connected) {
            player.displayClientMessage(TranslationUtil.message(Messages.NO_TUNNEL_SIDE), true);
            return false;
        }

        final var oldState = compactDim.getBlockState(position);
        compactDim.setBlock(position, tunnelState, Block.UPDATE_NEIGHBORS);

        if (compactDim.getBlockEntity(position) instanceof TunnelWallEntity twe) {
            final var connectedMachine = hist.getMachine();

            twe.setTunnelType(def);
            twe.setConnectedTo(hist.getMachine(), first);

            CompactMachinesNet.CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> compactDim.getChunkAt(position)),
                    new TunnelAddedPacket(position, def.getRegistryName(), hist.getMachine()));
        }

        compactDim.sendBlockUpdated(position, oldState, tunnelState, Block.UPDATE_ALL);
        return true;
    }
}
