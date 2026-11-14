package dev.compactmods.machines.room.generation;

import dev.compactmods.machines.api.room.generation.NewRoomBuilder;
import dev.compactmods.machines.api.room.generation.RoomGenerationDetails;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.core.util.MathUtil;
import dev.compactmods.spatial.aabb.AABBAligner;
import net.minecraft.core.Holder;

import java.util.UUID;

public class ServerNewRoomBuilder implements NewRoomBuilder {
    private final int spiralIndex;
    private MachineColor color = MachineColor.DEFAULT;

    private Holder<RoomTemplate> template;
    UUID owner;

    public ServerNewRoomBuilder(int spiralIndex) {
        this.spiralIndex = spiralIndex;
    }

    @Override
    public NewRoomBuilder template(Holder<RoomTemplate> template) {
        this.template = template;
        return this;
    }

    @Override
    public NewRoomBuilder template(RoomTemplate template) {
        this.template = Holder.direct(template);
        return this;
    }

    public ServerNewRoomBuilder owner(UUID owner) {
        this.owner = owner;
        return this;
    }

    private RoomBoundaries calculateBoundaries(RoomTemplate template) {
        final var region = MathUtil.getRegionPositionByIndex(spiralIndex);
        final var floor = MathUtil.getCenterWithY(region, 0);

        var outerBounds = AABBAligner.floor(template.getZeroBoundaries().move(floor), 0);
        return new RoomBoundaries(outerBounds);
    }

    @Override
    public RoomGenerationDetails build() {
        final var bounds = calculateBoundaries(template.value());
        return new RoomGenerationDetails(spiralIndex, template, bounds, owner);
    }
}
