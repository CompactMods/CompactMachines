package dev.compactmods.machines.room.upgrade.example;

import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeType;
import dev.compactmods.machines.api.room.upgrade.events.RoomUpgradeEvent;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeRemovedEventListener;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.block.SolidWallBlock;
import dev.compactmods.machines.room.upgrade.RoomUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class LightsOutUpgrade implements RoomUpgrade {
    public static final MapCodec<LightsOutUpgrade> CODEC = MapCodec.unit(LightsOutUpgrade::new);

    @Override
    public RoomUpgradeType<?> getType() {
        return RoomUpgrades.LIGHTS_OUT_UPGRADE.get();
    }

    @Override
    public Stream<RoomUpgradeEvent> gatherEvents() {
        final UpgradeRemovedEventListener removedEventListener = LightsOutUpgrade::lightsEventOn;
        final UpgradeAppliedEventListener addedEventListener = LightsOutUpgrade::lightsEventOff;
        return Stream.of(
                removedEventListener,
                addedEventListener
        );
    }

    public static void lightsEventOn(ServerLevel level, RoomInstance room, ItemStack upgrade) {
        // TURN ON LIGHTS
        var outer = room.boundaries().outerBounds();

        final var everythingLoaded = room.boundaries()
                .innerChunkPositions()
                .allMatch(cp -> level.shouldTickBlocksAt(cp.toLong()));

        // TODO - Implement upgrade cooldowns (i.e. retry in 100 ticks if room isn't loaded)
        if (!everythingLoaded) return;

        ServerLevel machineLevel = level.getServer().getLevel(CompactDimension.LEVEL_KEY);
        BlockPos.betweenClosedStream(outer)
                .filter(pos -> machineLevel.getBlockState(pos).is(Rooms.Blocks.SOLID_WALL))
                .forEach(pos ->
                        machineLevel.setBlock(pos, Rooms.Blocks.SOLID_WALL.get().defaultBlockState().setValue(SolidWallBlock.LIGHTS_ON, true), 3)
                );
    }

    public static void lightsEventOff(ServerLevel level, RoomInstance room, ItemStack upgrade) {
        // TURN OFF LIGHTS

        var outer = room.boundaries().outerBounds();

        final var everythingLoaded = room.boundaries()
                .innerChunkPositions()
                .allMatch(cp -> level.shouldTickBlocksAt(cp.toLong()));

        // TODO - Implement upgrade cooldowns (i.e. retry in 100 ticks if room isn't loaded)
        if (!everythingLoaded) return;

        ServerLevel machineLevel = level.getServer().getLevel(CompactDimension.LEVEL_KEY);
        BlockPos.betweenClosedStream(outer)
                .filter(pos -> machineLevel.getBlockState(pos).is(Rooms.Blocks.SOLID_WALL))
                .forEach(pos ->
                        machineLevel.setBlock(pos, Rooms.Blocks.SOLID_WALL.get().defaultBlockState().setValue(SolidWallBlock.LIGHTS_ON, false), 3)
                );
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(
                Component.literal(
                        "Lights Out Upgrade"
                )
        );
    }
}
