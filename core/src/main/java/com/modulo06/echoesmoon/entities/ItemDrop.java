package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;

public class ItemDrop {
    public Rectangle rect;
    public int type; // 0 = O2, 1 = Caixa de Componente (Crafting)

    public ItemDrop(float x, float y, int type) {
        this.rect = new Rectangle(x, y, 24, 24);
        this.type = type;
    }
}
