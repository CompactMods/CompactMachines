package dev.compactmods.machines.neoforge.client.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ImageButtonBuilder {

    private int x;
    private int y;
    private int width;
    private int height;
    private Component message;
    private Button.OnPress onPress;
    private final WidgetSprites sprites;

    private ImageButtonBuilder(WidgetSprites sprites) {
        this.x = 0;
        this.y = 0;
        this.width = 18;
        this.height = 18;
        this.message = CommonComponents.EMPTY;
        this.sprites = sprites;
    }

    public static ImageButtonBuilder button(WidgetSprites sprites) {
        return new ImageButtonBuilder(sprites);
    }

    public ImageButtonBuilder location(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public ImageButtonBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public ImageButtonBuilder message(Component message) {
        this.message = message;
        return this;
    }

    public ImageButtonBuilder onPress(Button.OnPress handler) {
        this.onPress = handler;
        return this;
    }

    public ImageButton build() {
        return new ImageButton(x, y, width, height, sprites, onPress, message);
    }
}