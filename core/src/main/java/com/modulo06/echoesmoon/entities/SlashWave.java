package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SlashWave {
    public Rectangle rect;
    public Vector2 velocity;
    public float angle;
    public boolean active = true;
    public float lifetime = 1.0f;

    // O tamanho físico do projétil é 48x48 para colisão, mas o sprite usará 64x64.
    public SlashWave(float x, float y, float targetX, float targetY) {
        rect = new Rectangle(x, y, 48, 48);
        Vector2 dir = new Vector2(targetX - (x + 24), targetY - (y + 24)).nor();
        velocity = dir.scl(700f); // Velocidade do tiro em linha reta
        angle = dir.angleDeg(); // Salva o ângulo para rotacionar o sprite depois
    }

    public void update(float delta) {
        rect.x += velocity.x * delta;
        rect.y += velocity.y * delta;
        lifetime -= delta;
        if (lifetime <= 0) active = false;
    }
}
