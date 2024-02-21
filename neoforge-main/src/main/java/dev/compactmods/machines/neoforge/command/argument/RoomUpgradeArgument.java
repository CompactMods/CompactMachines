//package dev.compactmods.machines.neoforge.command.argument;
//
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
//import dev.compactmods.machines.api.core.CMCommands;
//import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
//import dev.compactmods.machines.i18n.TranslationUtil;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.commands.arguments.ResourceKeyArgument;
//import net.minecraft.resources.ResourceKey;
//
//import java.util.Optional;
//
//public class RoomUpgradeArgument extends ResourceKeyArgument<RoomUpgrade> {
//
//    private static final DynamicCommandExceptionType ERROR_INVALID_UPGRADE = new DynamicCommandExceptionType((a) ->
//            TranslationUtil.command(CMCommands.WRONG_DIMENSION));
//
//    private RoomUpgradeArgument() {
//        super(RoomUpgrade.REG_KEY);
//    }
//
//    public static Optional<RoomUpgrade> getUpgrade(CommandContext<CommandSourceStack> stack, String argName) {
//        final var argKey = (ResourceKey<?>) stack.getArgument(argName, ResourceKey.class);
//        return argKey.cast(RoomUpgrade.REG_KEY)
//                .map(MachineRoomUpgrades.REGISTRY::get);
//    }
//
//    public static RoomUpgradeArgument upgrade() {
//        return new RoomUpgradeArgument();
//    }
//}
