package dev.compactmods.machines.forge.compat.theoneprobe.elements;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.client.PlayerFaceRenderer;
import mcjty.theoneprobe.api.IElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PlayerFaceElement implements IElement {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "player_face");
    private final GameProfile player;

    public PlayerFaceElement(GameProfile player) {
        this.player = player;
    }

    @Override
    public void render(PoseStack poseStack, int x, int y) {
        PlayerFaceRenderer.render(player, poseStack, x, y);
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeGameProfile(player);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public GameProfile player() {
        return player;
    }
}
