package org.dave.compactmachines3.command;

import com.google.gson.JsonObject;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.dave.compactmachines3.network.MessageClipboard;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.utility.SerializationHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRecipeCopyItem extends CommandBaseExt {
    @Override
    public String getName() {
        return "copy-item";
    }

    @Override
    public boolean isAllowed(EntityPlayer player, boolean creative, boolean isOp) {
        return creative || isOp;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP)sender.getCommandSenderEntity();

        if(args.length != 1 || (!args[0].equalsIgnoreCase("catalyst") && !args[0].equalsIgnoreCase("target"))) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-item.exception.missing_type"));
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-item.usage"));
            return;
        }

        String prefix = args[0].toLowerCase();
        ItemStack stack = player.getHeldItemMainhand();

        if(stack.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-item.exception.missing_item"));
            return;
        }

        String suffix = "item";
        if(stack.getItem() instanceof ItemBlock) {
            suffix = "block";
        }

        JsonObject resultObj = new JsonObject();
        if(prefix.equals("catalyst")) {
            resultObj.addProperty("catalyst", stack.getItem().getRegistryName().toString());
        } else {
            resultObj.addProperty(prefix + "-" + suffix, stack.getItem().getRegistryName().toString());
            resultObj.addProperty(prefix + "-count", stack.getCount());
        }

        resultObj.addProperty(prefix + "-meta", stack.getMetadata());

        if(stack.hasTagCompound()) {
            resultObj.addProperty(prefix + "-nbt", stack.getTagCompound().toString());
        }

        MessageClipboard message = new MessageClipboard();
        String rawJson = SerializationHelper.GSON.toJson(resultObj);
        message.setClipboardContent(rawJson.substring(1, rawJson.length()-2) + ",");
        PackageHandler.instance.sendTo(message, (EntityPlayerMP) sender.getCommandSenderEntity());

        player.sendMessage(new TextComponentTranslation("commands.compactmachines3.recipe.copy-item.success"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> options = Arrays.asList("target", "catalyst");
        return options.stream().filter(s -> args.length == 0 || s.startsWith(args[0])).collect(Collectors.toList());
    }
}
