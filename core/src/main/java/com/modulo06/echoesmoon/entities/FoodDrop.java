package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;

/** Comida encontrada no mapa. Nao cura automaticamente: vai para o inventario. */
public class FoodDrop {
    public final Rectangle rect;
    public FoodDrop(float x, float y) {
        rect = new Rectangle(x, y, 28f, 28f);
    }
}
