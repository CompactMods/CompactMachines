package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneTunnel;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.core.CompactMachinesNet;
import dev.compactmods.machines.tunnel.network.TunnelAddedPacket;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

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
            IForgeRegistry<TunnelDefinition> definitions = RegistryManager.ACTIVE.getRegistry(TunnelDefinition.class);
            definitions.getValues().forEach(def -> {
                if (def == Tunnels.UNKNOWN.get())
                    return;

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

        if (state.getBlock() instanceof SolidWallBlock && player != null) {
            getDefinition(context.getItemInHand()).ifPresent(def -> {
                try {
                    boolean success = setupTunnelWall(level, position, context.getClickedFace(), player, def);
                    if (success && !player.isCreative())
                        context.getItemInHand().shrink(1);
                } catch (Exception | MissingDimensionException e) {
                    CompactMachines.LOGGER.error(e);
                }
            });

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    public static Optional<IRoomHistoryItem> getMachineBindingInfo(Player player) {
        final LazyOptional<IRoomHistory> history = player.getCapability(Capabilities.ROOM_HISTORY);

        var mapped = history.resolve().map(hist -> {
            if (!hist.hasHistory() && player instanceof ServerPlayer sp) {
                PlayerUtil.howDidYouGetThere(sp);
                return null;
            }

            return hist.peek();
        }).orElse(null);

        return Optional.ofNullable(mapped);
    }

    public static Optional<IDimensionalPosition> getLastEnteredMachinePosition(Player player) {
        var lastEnteredMachine = getMachineBindingInfo(player);
        return lastEnteredMachine.flatMap(bound -> {
            try {
                CompactMachineData data = CompactMachineData.get(player.level.getServer());
                return data.getMachineLocation(bound.getMachine()).resolve();
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.fatal(e);
                return Optional.empty();
            }
        });
    }

    private static boolean setupTunnelWall(Level level, BlockPos position, Direction side, Player player, TunnelDefinition def) throws Exception, MissingDimensionException {
        boolean redstone = def instanceof IRedstoneTunnel;

        final var roomTunnels = RoomTunnelData.get(level.getServer(), new ChunkPos(position));
        final var tunnelGraph = roomTunnels.getGraph();

        var placedSides = tunnelGraph.getTunnelSides(def).collect(Collectors.toSet());

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

        var lastEnteredMachine = getMachineBindingInfo(player);
        if (lastEnteredMachine.isEmpty()) {
            CompactMachines.LOGGER.warn("Player does not appear to have entered room via a machine;" +
                    " history is empty. If this is an error, report it.");
            return false;
        }

        Direction first = newlyPlacedSide.get();
        var tunnelState = Tunnels.BLOCK_TUNNEL_WALL.get()
                .defaultBlockState()
                .setValue(TunnelWallBlock.TUNNEL_SIDE, side)
                .setValue(TunnelWallBlock.CONNECTED_SIDE, first)
                .setValue(TunnelWallBlock.REDSTONE, redstone);


        var hist = lastEnteredMachine.get();
        boolean connected = tunnelGraph.registerTunnel(position, def, hist.getMachine(), first);
        if (!connected) {
            player.displayClientMessage(TranslationUtil.message(Messages.NO_TUNNEL_SIDE), true);
            return false;
        }

        level.setBlock(position, tunnelState, Block.UPDATE_ALL_IMMEDIATE);

        if (level.getBlockEntity(position) instanceof TunnelWallEntity twe) {
            twe.setTunnelType(def);
            twe.setConnectedTo(hist.getMachine());

            CompactMachinesNet.CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(position)),
                    new TunnelAddedPacket(position, def));
        }

        return true;
    }
}
