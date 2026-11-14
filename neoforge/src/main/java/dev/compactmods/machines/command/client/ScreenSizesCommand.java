package dev.compactmods.machines.command.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ScreenSizesCommand {
    public static void registerScreenSizesCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        final var squareScreen = Commands.literal("square").executes(ctx -> {
            final var cl = Minecraft.getInstance();
            cl.getWindow().setWindowed(1920, 1920);
            return 0;
        });

        final var hdScreen = Commands.literal("hd").executes(ctx -> {
            final var cl = Minecraft.getInstance();
            cl.getWindow().setWindowed( 1920, 1080);
            return 0;
        });

        final var setScreenSizeCmd = Commands.literal("setScreenSize")
                .then(squareScreen)
                .then(hdScreen);

        dispatcher.register(setScreenSizeCmd);
    }
}
