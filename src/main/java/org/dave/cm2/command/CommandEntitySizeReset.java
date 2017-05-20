package org.dave.cm2.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.dave.cm2.miniaturization.MiniaturizationPotion;
import org.dave.cm2.network.MessageEntitySizeChange;
import org.dave.cm2.network.PackageHandler;

import javax.annotation.Nullable;
import java.util.List;

public class CommandEntitySizeReset extends CommandBaseExt {
    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityLivingBase ent;
        if(args.length == 0 && sender.getCommandSenderEntity() != null && sender.getCommandSenderEntity() instanceof EntityLivingBase) {
            ent = (EntityLivingBase) sender.getCommandSenderEntity();
        } else {
            ent = getEntity(server, sender, args[0], EntityLivingBase.class);
        }

        if(ent != null) {
            MiniaturizationPotion.setEntitySize(ent, 1.0f);
            PackageHandler.instance.sendToAll(new MessageEntitySizeChange(ent.getEntityId(), 1.0f));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if(args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());
        }
        return null;
    }
}
