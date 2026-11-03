package dev.compactmods.machines.datagen.models;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;

public class CMModelTemplates {

    public static final TextureSlot TINT_SLOT = TextureSlot.create("tint");
    public static final TextureSlot FRAME_SLOT = TextureSlot.create("frame");

    /**
     * Beacon-style "glass box": six 1px tinted panes inset into the frame
     * windows, wrapped by twelve thin black edge bars. tintindex 0 is fed by
     * {@link dev.compactmods.machines.machine.client.MachineColors#BLOCK} (and
     * the matching item tint source). Each pane is 14x14 across the face axis
     * and 1px deep, fitting flush with the cube surface but tucked between the
     * frame bars so the pane's side faces don't share a plane with the frame —
     * avoids z-fighting with the bars.
     */
    public static final ModelTemplate MACHINE_TEMPLATE = ModelTemplates.create(CompactMachinesCore.id("machine"),
                    TextureSlot.PARTICLE,
                    FRAME_SLOT,
                    TINT_SLOT)
            .extend()
            .parent(Identifier.withDefaultNamespace("block/block"))
            // Six glass panes, one per face. Outward face culls against neighbours;
            // inward face renders unconditionally so the tint reads from inside the
            // cube too. Side faces are intentionally omitted to keep them out of
            // any plane the frame bars occupy.
            .element(p -> pane(p, Direction.DOWN,  1, 0, 1,  15, 1, 15))
            .element(p -> pane(p, Direction.UP,    1, 15, 1, 15, 16, 15))
            .element(p -> pane(p, Direction.NORTH, 1, 1, 0,  15, 15, 1))
            .element(p -> pane(p, Direction.SOUTH, 1, 1, 15, 15, 15, 16))
            .element(p -> pane(p, Direction.WEST,  0, 1, 1,  1, 15, 15))
            .element(p -> pane(p, Direction.EAST,  15, 1, 1, 16, 15, 15))
            // Twelve 1px black edge bars forming the outer frame.
            // Bottom rim (Y = 0..1)
            .element(e -> edge(e, 0, 0, 0, 16, 1, 1))   // bottom-north
            .element(e -> edge(e, 0, 0, 15, 16, 1, 16)) // bottom-south
            .element(e -> edge(e, 0, 0, 0, 1, 1, 16))   // bottom-west
            .element(e -> edge(e, 15, 0, 0, 16, 1, 16)) // bottom-east
            // Top rim (Y = 15..16)
            .element(e -> edge(e, 0, 15, 0, 16, 16, 1))   // top-north
            .element(e -> edge(e, 0, 15, 15, 16, 16, 16)) // top-south
            .element(e -> edge(e, 0, 15, 0, 1, 16, 16))   // top-west
            .element(e -> edge(e, 15, 15, 0, 16, 16, 16)) // top-east
            // Vertical corner posts
            .element(e -> edge(e, 0, 0, 0, 1, 16, 1))     // nw
            .element(e -> edge(e, 15, 0, 0, 16, 16, 1))   // ne
            .element(e -> edge(e, 0, 0, 15, 1, 16, 16))   // sw
            .element(e -> edge(e, 15, 0, 15, 16, 16, 16)) // se
            .build();

    private static ElementBuilder edge(ElementBuilder e,
                                        int x1, int y1, int z1, int x2, int y2, int z2) {
        return e.from(x1, y1, z1)
                .to(x2, y2, z2)
                .allFaces((dir, face) -> face
                        .texture(FRAME_SLOT)
                        .uvs(0, 0, 16, 16));
    }

    private static ElementBuilder pane(ElementBuilder e, Direction outward,
                                        int x1, int y1, int z1, int x2, int y2, int z2) {
        return e.from(x1, y1, z1)
                .to(x2, y2, z2)
                .face(outward, f -> f
                        .texture(TINT_SLOT)
                        .uvs(0, 0, 16, 16)
                        .cullface(outward)
                        .tintindex(0))
                .face(outward.getOpposite(), f -> f
                        .texture(TINT_SLOT)
                        .uvs(0, 0, 16, 16)
                        .tintindex(0));
    }

    public static final TextureSlot WALL_SLOT = TextureSlot.create("wall");
    public static final TextureSlot TUNNEL_SLOT = TextureSlot.create("tunnel");

    /**
     * A solid wall with a tunnel overlay, layered like the grass block's side overlay. The overlay's
     * tintindex 0 is fed the tunnel type's color by {@code TunnelsClient}.
     */
    public static final ModelTemplate TUNNEL_WALL_TEMPLATE = ModelTemplates.create(CompactMachinesCore.id("tunnel_wall"),
                    TextureSlot.PARTICLE,
                    WALL_SLOT,
                    TUNNEL_SLOT)
            .extend()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .element(e -> e.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture(WALL_SLOT).uvs(0, 0, 16, 16).cullface(dir)))
            .element(e -> e.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((dir, face) -> face.texture(TUNNEL_SLOT).uvs(0, 0, 16, 16).cullface(dir).tintindex(0)))
            .build();

    public static final ModelTemplate ROOM_CORE_TEMPLATE = ModelTemplates.create(CompactMachinesCore.id("room_core"), TextureSlot.PARTICLE)
            .extend()

            .build();

    static {

    }

}
