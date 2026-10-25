package dev.compactmods.machines.upgrades.example;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.room.Rooms;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * The tree cutter's per-room state: whether it's enabled, which storages it takes axes from and puts drops into, the axe
 * it's cutting with (taken out of the tools storage, and worn here until it breaks), and the stumps
 * waiting to be replanted.
 * Owned by the tree cutter — filled from its canvas node (see
 * {@link TreeCutterUpgradeComponent#contribute}) and persisted in its own {@link #ATTACHMENT room
 * attachment}.
 */
public record TreeCutterState(boolean enabled, Optional<ItemHandlerAccess> tools, Optional<ItemHandlerAccess> drops,
                              ItemStack axe, List<BlockPos> stumps) {

    public static final TreeCutterState EMPTY =
            new TreeCutterState(false, Optional.empty(), Optional.empty(), ItemStack.EMPTY, List.of());

    /** Most stumps remembered; the oldest are forgotten past this (e.g. while out of saplings). */
    public static final int MAX_STUMPS = 64;

    public static final MapCodec<TreeCutterState> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.BOOL.optionalFieldOf("enabled", false).forGetter(TreeCutterState::enabled),
            ItemHandlerAccess.CODEC.optionalFieldOf("tools").forGetter(TreeCutterState::tools),
            ItemHandlerAccess.CODEC.optionalFieldOf("drops").forGetter(TreeCutterState::drops),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("axe", ItemStack.EMPTY).forGetter(TreeCutterState::axe),
            BlockPos.CODEC.listOf().optionalFieldOf("stumps", List.of()).forGetter(TreeCutterState::stumps)
    ).apply(i, TreeCutterState::new));

    public TreeCutterState {
        axe = axe.copy(); // never share a mutable stack with callers
        stumps = List.copyOf(stumps);
    }

    /** {@inheritDoc} Stacks compare by content, so an unchanged axe doesn't count as a change. */
    @Override
    public boolean equals(Object other) {
        return other instanceof TreeCutterState that
                && enabled == that.enabled && tools.equals(that.tools) && drops.equals(that.drops)
                && ItemStack.matches(axe, that.axe) && stumps.equals(that.stumps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, tools, drops, ItemStack.hashItemAndComponents(axe), axe.getCount(), stumps);
    }

    /** {@return a copy of the axe in use}, safe to wear and store back with {@link #withAxe} */
    @Override
    public ItemStack axe() {
        return axe.copy();
    }

    public static final Supplier<AttachmentType<TreeCutterState>> ATTACHMENT =
            Rooms.ATTACHMENT_TYPES.register("tree_cutter_upgrade", () -> AttachmentType
                    .builder(() -> EMPTY)
                    .serialize(MAP_CODEC)
                    .build());

    /** Loads the class so {@link #ATTACHMENT} is registered. Call during mod construction. */
    public static void init() {
    }

    public static TreeCutterState get(RoomInstance room) {
        return room.getData(ATTACHMENT);
    }

    /** Applies {@code change} to the room's state, storing it only if it actually changed. */
    public static void update(RoomInstance room, UnaryOperator<TreeCutterState> change) {
        final var current = get(room);
        final var updated = change.apply(current);
        if (!updated.equals(current))
            room.setData(ATTACHMENT, updated);
    }

    public TreeCutterState withEnabled(boolean enabled) {
        return new TreeCutterState(enabled, tools, drops, axe, stumps);
    }

    public TreeCutterState withTools(Optional<ItemHandlerAccess> tools) {
        return new TreeCutterState(enabled, tools, drops, axe, stumps);
    }

    public TreeCutterState withDrops(Optional<ItemHandlerAccess> drops) {
        return new TreeCutterState(enabled, tools, drops, axe, stumps);
    }

    public TreeCutterState withAxe(ItemStack axe) {
        return new TreeCutterState(enabled, tools, drops, axe, stumps);
    }

    public TreeCutterState withStumps(List<BlockPos> stumps) {
        return new TreeCutterState(enabled, tools, drops, axe, stumps);
    }

    /** The tools storage, if set and loaded. */
    public Optional<ResourceHandler<ItemResource>> toolsHandler(MinecraftServer server) {
        return tools.flatMap(access -> resolve(server, access));
    }

    /** The drops storage, if set and loaded. */
    public Optional<ResourceHandler<ItemResource>> dropsHandler(MinecraftServer server) {
        return drops.flatMap(access -> resolve(server, access));
    }

    private static Optional<ResourceHandler<ItemResource>> resolve(MinecraftServer server, ItemHandlerAccess access) {
        return access.resolve(server);
    }
}
