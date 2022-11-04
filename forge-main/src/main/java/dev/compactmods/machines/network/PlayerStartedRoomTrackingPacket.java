package dev.compactmods.machines.network;

import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public record PlayerStartedRoomTrackingPacket(String room) {

    public PlayerStartedRoomTrackingPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        var sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            StructureTemplate blocks = null;
            try {
                blocks = Rooms.getInternalBlocks(sender.server, room).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            } catch (MissingDimensionException e) {
                throw new RuntimeException(e);
            } catch (NonexistentRoomException e) {
                throw new RuntimeException(e);
            }
            RoomNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new InitialRoomBlockDataPacket(blocks));
        });

        return true;
    }
}
