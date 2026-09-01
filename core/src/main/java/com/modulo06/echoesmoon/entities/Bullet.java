package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    public Rectangle rect;
    public Vector2 velocity;
    public boolean active = true;
    public float lifetime = 2.0f; // Tempo de vida do tiro antes de sumir

    public Bullet(float x, float y, float dirX, float dirY) {
        rect = new Rectangle(x, y, 8, 8);
        float speed = 650f;
        if (dirX == 0 && dirY == 0) dirX = 1f; // Direção padrão se parado
        velocity = new Vector2(dirX, dirY).nor().scl(speed);
    }

    public void update(float delta) {
        rect.x += velocity.x * delta;
        rect.y += velocity.y * delta;
        lifetime -= delta;
        if (lifetime <= 0) active = false;
    }
}
