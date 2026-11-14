package dev.compactmods.machines.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.UnaryOperator;

public class CompactMachinesCore {

    public final static String MOD_ID = "compactmachines";

    public static final LiteralArgumentBuilder<CommandSourceStack> CM_COMMAND_ROOT
            = LiteralArgumentBuilder.literal(CompactMachinesCore.MOD_ID);

    public static String id(String path) {
        return Identifier.isValidPath(path) ? (MOD_ID + ":" + path) : MOD_ID + ":invalid";
    }

    public static String dotPrefix(String path) {
        return MOD_ID + "." + path;
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Logger modLog() {
        return LogManager.getLogger(MOD_ID);
    }

    public static Item basicItem(UnaryOperator<Item.Properties> moreProps) {
		return new Item(moreProps.apply(new Item.Properties()));
	}
}
