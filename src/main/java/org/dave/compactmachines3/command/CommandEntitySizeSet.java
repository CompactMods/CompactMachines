package org.dave.compactmachines3.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.dave.compactmachines3.miniaturization.MiniaturizationPotion;
import org.dave.compactmachines3.network.MessageEntitySizeChange;
import org.dave.compactmachines3.network.PackageHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CommandEntitySizeSet extends CommandBaseExt {
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 0) {
            throw this.getUsageException(sender);
        }

        EntityLivingBase ent;
        float scale;
        if(args.length == 1 && sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityLivingBase) {
            ent = (EntityLivingBase) sender.getCommandSenderEntity();
            scale = Float.parseFloat(args[0]);
        } else {
            ent = getEntity(server, sender, args[0], EntityLivingBase.class);
            try {
                scale = Float.parseFloat(args[1]);
            } catch(Exception e) {
                throw this.getUsageException(sender);
            }
        }

        if(ent != null) {
            MiniaturizationPotion.setEntitySize(ent, scale);
            PackageHandler.instance.sendToAll(new MessageEntitySizeChange(ent.getEntityId(), scale));
        } else {
            throw this.getUsageException(sender);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if(args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());
        }
        if(args.length == 2) {
            return Arrays.asList("1.0");
        }
        return null;
    }
}
