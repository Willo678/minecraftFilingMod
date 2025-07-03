package net.willo678.filingcabinet.container;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class BaseButton extends Button {

    public BaseButton(int x, int y, int w, int h, Component text, OnPress onPress) {
        super(x, y, w, h, text, onPress);
    }

    public void setX(int i) {
        x = i;
    }

    public void setY(int i) {
        y = i;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
