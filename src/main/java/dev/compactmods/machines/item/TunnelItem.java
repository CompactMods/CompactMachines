package dev.compactmods.machines.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneTunnel;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import dev.compactmods.machines.block.walls.SolidWallBlock;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.network.NetworkHandler;
import dev.compactmods.machines.network.TunnelAddedPacket;
import dev.compactmods.machines.rooms.capability.IRoomHistory;
import dev.compactmods.machines.tunnel.TunnelHelper;
import dev.compactmods.machines.tunnel.UnknownTunnel;
import dev.compactmods.machines.util.PlayerUtil;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
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
                if (def instanceof UnknownTunnel)
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

        if (state.getBlock() instanceof SolidWallBlock && player instanceof ServerPlayer serverPlayer) {
            getDefinition(context.getItemInHand()).ifPresent(def -> {
                try {
                    boolean success = setupTunnelWall(level, position, context.getClickedFace(), serverPlayer, def);
                    if (success && !player.isCreative())
                        context.getItemInHand().shrink(1);
                } catch (Exception e) {
                    CompactMachines.LOGGER.error(e);
                }
            });

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    private static boolean setupTunnelWall(Level level, BlockPos position, Direction side, ServerPlayer player, TunnelDefinition def) throws Exception {
        boolean redstone = def instanceof IRedstoneTunnel;
        final LazyOptional<IRoomHistory> history = player.getCapability(Capabilities.ROOM_HISTORY);
        if (!history.isPresent()) {
            PlayerUtil.howDidYouGetThere(player);
            return false;
        }

        var hist = history.orElseThrow(() -> new Exception("Player machine history not found. If this is an error, report it."));
        if (!hist.hasHistory()) {
            PlayerUtil.howDidYouGetThere(player);
            return false;
        }

        int enteredThrough = hist.peek().getMachine();
        var data = CompactMachineData.get(player.server);
        var machLoc = data.getMachineLocation(enteredThrough);
        if (!machLoc.isPresent()) {
            CompactMachines.LOGGER.warn("Player does not appear to have entered room via a machine;" +
                    " history is empty. If this is an error, report it.");
            return false;
        }

        var chunk = level.getChunkAt(position);
        return chunk.getCapability(Capabilities.ROOM_TUNNELS).map(room -> {
            final Set<TunnelWallEntity> existingOfType = room.stream(def)
                    .map(level::getBlockEntity)
                    .filter(be -> be instanceof TunnelWallEntity)
                    .map(be -> (TunnelWallEntity) be)
                    .collect(Collectors.toSet());

            final Set<Direction> sides = existingOfType.stream()
                    .map(TunnelWallEntity::getTunnelSide)
                    .collect(Collectors.toSet());

            Optional<Direction> firstAvailable = TunnelHelper.getOrderedSides()
                    .filter(s -> !sides.contains(s))
                    .findFirst();

            if (firstAvailable.isEmpty()) {
                player.sendMessage(TranslationUtil.message(Messages.NO_TUNNEL_SIDE).withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);
                return false;
            }

            Direction first = firstAvailable.get();
            var tunnelState = Tunnels.BLOCK_TUNNEL_WALL.get()
                    .defaultBlockState()
                    .setValue(TunnelWallBlock.TUNNEL_SIDE, side)
                    .setValue(TunnelWallBlock.CONNECTED_SIDE, first)
                    .setValue(TunnelWallBlock.REDSTONE, redstone);

            level.setBlock(position, tunnelState, Block.UPDATE_ALL);

            chunk.getCapability(Capabilities.ROOM_TUNNELS).ifPresent(roomTunn -> {
                roomTunn.register(def, position);
            });

            if (level.getBlockEntity(position) instanceof TunnelWallEntity tun) {
                try {
                    tun.setTunnelType(def);
                    tun.setConnectedTo(enteredThrough);

                    level.getServer().submitAsync(() -> {
                        NetworkHandler.MAIN_CHANNEL.send(
                                PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(position)),
                                new TunnelAddedPacket(position, def));
                    });
                } catch (Exception ignored) {
                }
            }
            return true;
        }).orElse(false);
    }
}
