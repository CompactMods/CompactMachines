package dev.compactmods.machines.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.TunnelMachineInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CompactMachineTooltipData(
        boolean hasPlayers,
        Optional<UUID> owner,
        Optional<ChunkPos> connectedRoom,
        boolean hasTunnels,
        List<TunnelMachineInfo> connectedTunnels
) {

    public static final Codec<CompactMachineTooltipData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("hasPlayers")
                    .forGetter(CompactMachineTooltipData::hasPlayers),

            CodecExtensions.UUID_CODEC.optionalFieldOf("owner")
                    .forGetter(CompactMachineTooltipData::owner),

            CodecExtensions.CHUNKPOS.optionalFieldOf("connectedRoom")
                    .forGetter(CompactMachineTooltipData::connectedRoom),

            Codec.BOOL.fieldOf("hasTunnels")
                    .forGetter(CompactMachineTooltipData::hasTunnels),

            TunnelMachineInfo.CODEC.listOf()
                    .fieldOf("tunnels")
                    .forGetter(CompactMachineTooltipData::connectedTunnels)
    ).apply(i, CompactMachineTooltipData::new));

    public static CompactMachineTooltipData forMachine(ServerLevel compactDim, CompactMachineBlockEntity machine) {
        final var boundTo = machine.getConnectedRoom();
        final var owner = machine.getOwnerUUID();

        List<TunnelMachineInfo> tunnels = new ArrayList<>();
        boundTo.ifPresent(room -> {
            final var graph = TunnelConnectionGraph.forRoom(compactDim, room);
            final var applied = graph.tunnels(machine.getLevelPosition())
                    .toList();

            tunnels.addAll(applied);
        });

        return new CompactMachineTooltipData(
                machine.hasPlayersInside(),
                owner,
                boundTo,
                machine.hasTunnels(),
                tunnels
        );
    }
}
