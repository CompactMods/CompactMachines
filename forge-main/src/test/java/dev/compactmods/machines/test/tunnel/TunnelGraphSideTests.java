package dev.compactmods.machines.test.tunnel;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class TunnelGraphSideTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void unregisteredMachineConnectionCheck(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        graph.register(BlockPos.ZERO, FakeTunnelDefinition.ID, MACHINE_POS, Direction.UP);

        final var typesForUp = graph.types(MACHINE_POS, Direction.UP)
                .collect(Collectors.toUnmodifiableSet());

        if(typesForUp.size() != 1) {
            test.fail("Expected the machine to have one tunnel registered for UP; got %s".formatted(typesForUp.size()));
        }

        if(!typesForUp.contains(FakeTunnelDefinition.ID)) {
            test.fail("Expected the fake tunnel definition to be registered for UP");
        }

        // Check side of tunnel
        graph.side(BlockPos.ZERO).ifPresentOrElse(side -> {
            if(side != Direction.UP)
                test.fail("Expected tunnel side to be UP");
        }, () -> {
            test.fail("Expected tunnel side to be set.");
        });

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.TUNNEL_DATA)
    public static void canLookupSidesByDefinition(final GameTestHelper test) {
        final var graph = new TunnelConnectionGraph();
        final var MACHINE_POS = GlobalPos.of(test.getLevel().dimension(), BlockPos.ZERO);

        graph.register(BlockPos.ZERO, FakeTunnelDefinition.ID, MACHINE_POS, Direction.UP);

        final var sides = graph.sides(MACHINE_POS, FakeTunnelDefinition.ID)
                .collect(Collectors.toSet());

        if(sides.size() != 1) {
            test.fail("Expected one side matched; got %s".formatted(sides.size()));
        }

        if(!sides.contains(Direction.UP)) {
            var sidesString = sides.stream()
                            .map(Direction::getName)
                            .collect(Collectors.joining(","));

            test.fail("Expected fake tunnel side lookup to include UP; it did not. Contents: %s".formatted(sidesString));
        }

        test.succeed();
    }
}
