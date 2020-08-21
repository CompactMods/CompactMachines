package org.dave.compactmachines3.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.TeleporterMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;
import org.dave.compactmachines3.world.tools.DimensionTools;
import org.dave.compactmachines3.world.tools.TeleportationTools;

public class MessageRequestMachineActionHandler implements IMessageHandler<MessageRequestMachineAction, MessageMachineContent> {
    @Override
    public MessageMachineContent onMessage(MessageRequestMachineAction message, MessageContext ctx) {
        int coords = message.coords;
        if (WorldSavedDataMachines.INSTANCE == null) {
            return null;
        }
        if(message.action == MessageRequestMachineAction.Action.REFRESH) {
            if(message.coords < 0) {
                coords = WorldSavedDataMachines.INSTANCE.nextCoord-1;
            }
            if(message.coords >= WorldSavedDataMachines.INSTANCE.nextCoord) {
                coords = 0;
            }
        }

        final boolean[] updateMachineContent = {true};
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

        if(message.action == MessageRequestMachineAction.Action.TOGGLE_LOCKED) {
            serverPlayer.getServerWorld().addScheduledTask(() -> {
                DimensionBlockPos pos = WorldSavedDataMachines.INSTANCE.machinePositions.get(finalCoords);
                TileEntity te = DimensionTools.getWorldServerForDimension(pos.getDimension()).getTileEntity(pos.getBlockPos());
                if (te != null && te instanceof TileEntityMachine) {
                    TileEntityMachine machine = (TileEntityMachine) te;
                    machine.toggleLocked();
                    machine.markDirty();
                }
            });
        }

        if(message.action == MessageRequestMachineAction.Action.TRY_TO_ENTER) {
            serverPlayer.getServerWorld().addScheduledTask(() -> {
                DimensionBlockPos pos = WorldSavedDataMachines.INSTANCE.machinePositions.get(finalCoords);
                TileEntity te = DimensionTools.getWorldServerForDimension(pos.getDimension()).getTileEntity(pos.getBlockPos());
                if (te != null && te instanceof TileEntityMachine) {
                    TileEntityMachine machine = (TileEntityMachine) te;
                    TeleportationTools.tryToEnterMachine(serverPlayer, machine);
                    updateMachineContent[0] = false;
                }
            });
        }

        if(updateMachineContent[0]) {
            serverPlayer.getServerWorld().addScheduledTask(() -> {
                PackageHandler.instance.sendTo(new MessageMachineChunk(finalCoords), serverPlayer);
                PackageHandler.instance.sendTo(new MessageMachineContent(finalCoords), serverPlayer);
            });
        }

        return null;
    }
}
