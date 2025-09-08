//package dev.compactmods.machines.compat.jade;
//
//import com.mojang.authlib.GameProfile;
//import dev.compactmods.machines.client.render.CMPlayerFaceRenderer;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.world.phys.Vec2;
//import snownee.jade.api.ui.Element;
//
//public class PlayerFaceElement extends Element {
//
//    private final GameProfile profile;
//
//    public PlayerFaceElement(GameProfile profile) {
//        this.profile = profile;
//        this.size = new Vec2(12, 12);
//        this.translation = Vec2.ZERO;
//        this.message = null;
//    }
//
//    @Override
//    public Vec2 getSize() {
//        return size;
//    }
//
//    @Override
//    public void render(GuiGraphics guiGraphics, float x, float y, float xMax, float yMax) {
//        CMPlayerFaceRenderer.render(profile, guiGraphics, (int) x, (int) y, (int) size.x);
//    }
//}
