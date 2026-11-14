package dev.compactmods.machines.datagen.basic_room_templates;


import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.machine.MachineColor;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

public final class RoomTemplatesGenerator {
    static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(RoomTemplate.REGISTRY_KEY, RoomTemplatesGenerator::addRoomTemplates);

    private RoomTemplatesGenerator() {}

    private static void addRoomTemplates(BootstrapContext<RoomTemplate> ctx) {
        roomTemplate(ctx, "tiny", new RoomTemplate(3, ARGB.color(255, 201, 91, 19)));
        roomTemplate(ctx, "small", new RoomTemplate(5, ARGB.color(255, 212, 210, 210)));
        roomTemplate(ctx, "normal", new RoomTemplate(7, ARGB.color(255, 251, 242, 54)));
        roomTemplate(ctx, "large", new RoomTemplate(9, ARGB.color(255, 33, 27, 46)));
        roomTemplate(ctx, "giant", new RoomTemplate(11, ARGB.color(255, 67, 214, 205)));
        roomTemplate(ctx, "colossal", new RoomTemplate(13, ARGB.color(255, 66, 63, 66)));

        roomTemplate(ctx, "soaryn", new RoomTemplate(45, DyeColor.PURPLE.getFireworkColor()));
        roomTemplate(ctx, "farming", RoomTemplate.builder()
                .withInternalSize(21, 21, 11)
                .defaultMachineColor(MachineColor.fromARGB(CommonColors.GREEN))
                .withFloor(Blocks.GRASS_BLOCK.defaultBlockState())
                .build());
    }

    private static void roomTemplate(BootstrapContext<RoomTemplate> ctx, String name, RoomTemplate template) {
        ctx.register(ResourceKey.create(RoomTemplate.REGISTRY_KEY, CompactMachinesCore.identifier(name)), template);
    }
}