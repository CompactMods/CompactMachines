package dev.compactmods.machines.upgrades.storage.tunnel.menu;

import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The tunnel config screen's menu, for the wall at {@link #wallPos()}. In create mode ({@link #state()}
 * empty) it names a new storage and picks its tunnel type; in edit mode it renames the bound storage.
 *
 * <p>Its one slot holds container items (chests, buckets, ...) that buy the storage capacity; they're
 * only taken when a submission succeeds, and handed back if the screen is closed.
 */
public class TunnelConfigMenu extends AbstractContainerMenu {

    private static final int REACH = 5;

    private final BlockPos wallPos;
    private final Direction face;
    private final Slot containerSlot;
    private final ItemStacksResourceHandler containerHandler;

    private Optional<TunnelConfigState> state;
    private Optional<Component> error = Optional.empty();
    private int revision;

    public TunnelConfigMenu(int containerId, Inventory inventory, BlockPos wallPos, Direction face, Optional<TunnelConfigState> state) {
        super(Tunnels.CONFIG_MENU.get(), containerId);
        this.wallPos = wallPos;
        this.face = face;
        this.state = state;

        final var containers = containerItems(inventory.player);
        this.containerHandler = new ItemStacksResourceHandler(1) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                return containers.contains(resource.getItem());
            }
        };

        this.addStandardInventorySlots(inventory, 10, 84);
        this.containerSlot = this.addSlot(new ResourceHandlerSlot(containerHandler, containerHandler::set, 0, 10, 10));
    }

    public TunnelConfigMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readBlockPos(), extraData.readEnum(Direction.class),
                ByteBufCodecs.optional(TunnelConfigState.STREAM_CODEC).decode(extraData));
    }

    /** Client-side data for the menu, read by the buffer constructor. Called by the menu's provider. */
    public static void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        final var tunnelMenu = (TunnelConfigMenu) menu;
        buffer.writeBlockPos(tunnelMenu.wallPos);
        buffer.writeEnum(tunnelMenu.face);
        ByteBufCodecs.optional(TunnelConfigState.STREAM_CODEC).encode(buffer, tunnelMenu.state);
    }

    /** Every tunnel type's container item, from the (synced) tunnel type registry. */
    private static Set<Item> containerItems(Player player) {
        return player.level().registryAccess().lookup(TunnelType.REGISTRY_KEY)
                .map(types -> types.listElements()
                        .map(type -> type.value().container().value())
                        .collect(Collectors.toUnmodifiableSet()))
                .orElse(Set.of());
    }

    public BlockPos wallPos() {
        return wallPos;
    }

    public Direction face() {
        return face;
    }

    public Slot containerSlot() {
        return containerSlot;
    }

    /** The bound storage in edit mode; empty in create mode. */
    public Optional<TunnelConfigState> state() {
        return state;
    }

    /** Why the last submission was refused, if it was. */
    public Optional<Component> error() {
        return error;
    }

    /** Bumped on every {@link #update}, so the screen can tell when to refresh. */
    public int revision() {
        return revision;
    }

    /** Applies a submission's outcome: the storage as it now stands (if it succeeded) and/or why it failed. */
    public void update(Optional<TunnelConfigState> state, Optional<Component> error) {
        if (state.isPresent())
            this.state = state;
        this.error = error;
        this.revision++;
    }

    /** {@return how many of {@code container} are in the slot} */
    public int containerCount(Item container) {
        final var stack = containerSlot.getItem();
        return stack.is(container) ? stack.getCount() : 0;
    }

    /** Takes {@code count} container items out of the slot, once a submission has used them. */
    public void takeContainers(int count) {
        if (count > 0)
            containerSlot.remove(count);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        final var slot = this.slots.get(index);
        if (!slot.hasItem())
            return ItemStack.EMPTY;

        final var stack = slot.getItem();
        final var original = stack.copy();
        final int container = containerSlot.index;

        if (index == container) {
            if (!this.moveItemStackTo(stack, 0, container, true))
                return ItemStack.EMPTY;
        } else if (!containerSlot.mayPlace(stack) || !this.moveItemStackTo(stack, container, container + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        final var wall = player.level().getBlockState(wallPos);
        return (wall.is(Rooms.Blocks.SOLID_WALL.get()) || wall.is(Tunnels.Blocks.TUNNEL_WALL.get()))
                && player.isWithinBlockInteractionRange(wallPos, REACH);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide())
            return;

        // Hand back anything left in the slot
        final var leftover = containerSlot.getItem();
        if (!leftover.isEmpty()) {
            containerSlot.set(ItemStack.EMPTY);
            if (player.isAlive() && !(player instanceof ServerPlayer sp && sp.hasDisconnected()))
                player.getInventory().placeItemBackInInventory(leftover, Prediction.SERVER_ONLY);
            else
                player.drop(leftover, false, Prediction.SERVER_ONLY);
        }
    }
}
