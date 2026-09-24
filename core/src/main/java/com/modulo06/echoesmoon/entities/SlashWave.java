package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SlashWave {
    public Rectangle rect;
    public Vector2 velocity;
    public float angle;
    public boolean active = true;
    public float lifetime = 1.0f;
    public float damageMultiplier = 1f;

    // O tamanho físico do projétil é 48x48 para colisão, mas o sprite usará 64x64.
    public SlashWave(float x, float y, float targetX, float targetY) {
        this(x, y, targetX, targetY, false);
    }

    public SlashWave(float x, float y, float targetX, float targetY, boolean charged) {
        float size = charged ? 72f : 48f;
        rect = new Rectangle(x - (size - 48f) * 0.5f, y - (size - 48f) * 0.5f, size, size);
        Vector2 dir = new Vector2(targetX - (x + 24), targetY - (y + 24)).nor();
        velocity = dir.scl(charged ? 820f : 700f); // Velocidade do tiro em linha reta
        damageMultiplier = charged ? 3.0f : 1f;
        angle = dir.angleDeg(); // Salva o ângulo para rotacionar o sprite depois
    }

    public void update(float delta) {
        rect.x += velocity.x * delta;
        rect.y += velocity.y * delta;
        lifetime -= delta;
        if (lifetime <= 0) active = false;
    }
}
