package org.dave.compactmachines3.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.TeleporterMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.TeleportationTools;

public class MessageRequestMachineActionHandler implements IMessageHandler<MessageRequestMachineAction, MessageMachineContent> {
    @Override
    public MessageMachineContent onMessage(MessageRequestMachineAction message, MessageContext ctx) {
        int coords = message.coords;
        if(message.coords < 0) {
            coords = WorldSavedDataMachines.INSTANCE.nextCoord-1;
        }
        if(message.coords >= WorldSavedDataMachines.INSTANCE.nextCoord) {
            coords = 0;
        }

        int finalCoords = coords;
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        if(message.action == MessageRequestMachineAction.Action.GIVE_ITEM) {
            EnumMachineSize size = WorldSavedDataMachines.INSTANCE.machineSizes.get(coords);

            serverPlayer.getServerWorld().addScheduledTask(() -> {
                ItemStack stack = new ItemStack(Blockss.machine, 1, size.getMeta());
                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("coords", finalCoords);
                stack.setTagCompound(compound);

                ItemHandlerHelper.giveItemToPlayer(serverPlayer, stack);
                WorldSavedDataMachines.INSTANCE.removeMachinePosition(finalCoords);
            });
        }

        if(message.action == MessageRequestMachineAction.Action.TELEPORT_INSIDE) {
            TeleportationTools.teleportPlayerToMachine(serverPlayer, coords, false);
            // return null;
        }

        if(message.action == MessageRequestMachineAction.Action.TELEPORT_OUTSIDE) {
            DimensionBlockPos pos = WorldSavedDataMachines.INSTANCE.machinePositions.get(coords);
            WorldServer world = DimensionTools.getWorldServerForDimension(pos.getDimension());
            BlockPos spawnPos = TeleportationTools.getValidSpawnLocation(world, pos.getBlockPos());

            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            playerList.transferPlayerToDimension(serverPlayer, pos.getDimension(), new TeleporterMachines(world));
            serverPlayer.setPositionAndUpdate(spawnPos.getX() + 0.5f, spawnPos.getY() + 0.5f, spawnPos.getZ() + 0.5f);
        }

        serverPlayer.getServerWorld().addScheduledTask(() -> {
            PackageHandler.instance.sendTo(new MessageMachineContent(finalCoords), serverPlayer);
        });

        return null;
    }
}
