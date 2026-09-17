package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.modulo06.echoesmoon.systems.Inventario;

/** Boss de Calisto com 3 formas e uma unica entidade. */
public class BossCalisto {
    public int forma = 1;
    public float hp = 100f;
    public float hpMax = 100f;
    public float speed = 90f;
    public boolean mortoFinal = false;

    public float x = 400f;
    public float y = 300f;
    public float width = 64f;
    public float height = 64f;

    private float dashTimer = 0f;
    private static final float DASH_COOLDOWN = 3.0f;
    private boolean isDashing = false;
    private float dashDuration = 0.20f;
    private float currentDashTime = 0f;

    public void update(float delta, float playerX, float playerY, Inventario inventario) {
        if (mortoFinal) return;

        if (forma == 3) {
            dashTimer += delta;
            if (dashTimer >= DASH_COOLDOWN) {
                dashTimer = 0f;
                isDashing = true;
                currentDashTime = 0f;
            }
            if (isDashing) {
                currentDashTime += delta;
                if (currentDashTime >= dashDuration) {
                    isDashing = false;
                }
            }
        }

        float currentSpeed = isDashing ? speed * 2.5f : speed;
        float dirX = playerX - x;
        float dirY = playerY - y;
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0.001f) {
            x += (dirX / len) * currentSpeed * delta;
            y += (dirY / len) * currentSpeed * delta;
        }
    }

    /**
     * Aplica dano e garante que a terceira queda mate definitivamente o boss.
     * Ao trocar de forma, a municao recebe um pequeno reabastecimento para
     * impedir que a luta fique travada por falta de tiros.
     */
    public void levarDano(float dano, Inventario inventario) {
        if (mortoFinal || dano <= 0f) return;

        hp = Math.max(0f, hp - dano);
        if (hp > 0f) return;

        if (forma < 3) {
            forma++;
            hpMax = (forma == 2) ? 150f : 220f;
            hp = hpMax;
            speed = (forma == 2) ? 110f : 130f;
            width += 12f;
            height += 12f;
            dashTimer = 0f;
            isDashing = false;

            if (inventario != null) {
                inventario.municao = Math.max(inventario.municao, 20);
            }
            return;
        }

        // Terceira morte: acabou a luta.
        hp = 0f;
        mortoFinal = true;
        if (inventario != null) {
            inventario.add("CHAVE_LUZ");
        }
    }

    public boolean isDerrotado() {
        return mortoFinal;
    }

    public void render(ShapeRenderer shape) {
        if (mortoFinal) return;

        if (forma == 1) shape.setColor(Color.MAGENTA);
        else if (forma == 2) shape.setColor(Color.ORANGE);
        else shape.setColor(isDashing ? Color.WHITE : Color.RED);

        shape.rect(x, y, width, height);
    }
}
