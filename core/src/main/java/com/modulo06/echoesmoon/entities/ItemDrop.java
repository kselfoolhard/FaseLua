package com.modulo06.echoesmoon.entities;
import com.badlogic.gdx.math.Rectangle;

public class ItemDrop {
    public Rectangle rect;
    public int tipo; // 0 = O2, 1 = Comida

    public ItemDrop(float x, float y, int tipo) {
        this.rect = new Rectangle(x, y, 24, 24);
        this.tipo = tipo;
    }
}
