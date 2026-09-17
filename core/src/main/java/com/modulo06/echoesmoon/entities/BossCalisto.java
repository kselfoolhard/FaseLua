package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.modulo06.echoesmoon.systems.Inventario;

public class BossCalisto {
    public int forma = 1;
    public float hp = 100f;
    public float hpMax = 100f;
    public float speed = 90f;
    public boolean mortoFinal = false;

    public float x = 400;
    public float y = 300;
    public float width = 64;
    public float height = 64;

    private float dashTimer = 0f;
    private float dashCooldown = 3.0f;
    private boolean isDashing = false;
    private float dashDuration = 0.2f;
    private float currentDashTime = 0f;

    public void update(float delta, float playerX, float playerY, Inventario inventario) {
        if (mortoFinal) return;

        // Lógica da Forma 3: Dash a cada 3 segundos
        if (forma == 3) {
            dashTimer += delta;
            if (dashTimer >= dashCooldown) {
                isDashing = true;
                dashTimer = 0f;
                currentDashTime = 0f;
            }

            if (isDashing) {
                currentDashTime += delta;
                if (currentDashTime >= dashDuration) {
                    isDashing = false;
                }
            }
        }

        // Perseguição do jogador
        float currentSpeed = isDashing ? speed * 2.5f : speed;
        float dirX = playerX - x;
        float dirY = playerY - y;
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (len > 0) {
            x += (dirX / len) * currentSpeed * delta;
            y += (dirY / len) * currentSpeed * delta;
        }
    }

    public void levarDano(float dano, Inventario inventario) {
        if (mortoFinal) return;

        hp -= dano;

        if (hp <= 0) {
            if (forma < 3) {
                forma++;
                hpMax = hpMax * 1.5f; // Aumento progressivo de HP
                hp = hpMax;
                speed *= 1.18f;       // Aumento de velocidade
                width += 12;          // Aumenta o tamanho visual a cada forma
                height += 12;
            } else {
                mortoFinal = true;
                hp = 0;
                inventario.add("CHAVE_LUZ"); // Drop obrigatório ao morrer
            }
        }
    }

    public void render(ShapeRenderer shape) {
        if (mortoFinal) return;

        // Troca de cor por forma (mutação visual)
        if (forma == 1) shape.setColor(Color.MAGENTA);
        else if (forma == 2) shape.setColor(Color.ORANGE);
        else if (forma == 3) shape.setColor(isDashing ? Color.WHITE : Color.RED);

        shape.rect(x, y, width, height);
    }
}
