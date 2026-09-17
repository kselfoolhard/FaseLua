package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Versao de entidade reutilizavel do boss de Titan. */
public class BossTita {
    public static final int CHASE = 0;
    public static final int WINDUP = 1;

    public final Rectangle rect;
    public final int maxHp = 180;
    public int hp = maxHp;
    public final float speed = 145f;
    public boolean ativo = true;
    private int state = CHASE;
    private float stateTimer = 0f;
    private boolean rockReady = false;
    private final Vector2 rockTarget = new Vector2();

    public BossTita(float x, float y) { rect = new Rectangle(x, y, 88f, 88f); }

    public void update(float delta, Vector2 playerPos) {
        if (!ativo) return;
        stateTimer += delta;
        if (state == CHASE) {
            Vector2 dir = new Vector2(playerPos).sub(rect.x, rect.y);
            if (dir.len() > 155f) {
                dir.nor(); rect.x += dir.x * speed * delta; rect.y += dir.y * speed * delta;
            }
            if (stateTimer >= 2.8f) {
                state = WINDUP; stateTimer = 0f; rockTarget.set(playerPos.x + 16f, playerPos.y + 20f);
            }
        } else if (state == WINDUP && stateTimer >= 0.45f) {
            rockReady = true; state = CHASE; stateTimer = 0f;
        }
    }

    public boolean consumeRockThrow() { if (!rockReady) return false; rockReady = false; return true; }
    public Vector2 getRockTarget() { return rockTarget; }
    public int getRockDamage() { return 15; }
    public int getContactDamage() { return 14; }

    public void levarDano(int dano) { if (!ativo) return; hp = Math.max(0, hp - dano); if (hp == 0) ativo = false; }

    public void renderFallback(ShapeRenderer shape) {
        if (!ativo) return;
        shape.setColor(state == WINDUP ? Color.YELLOW : Color.RED);
        shape.rect(rect.x, rect.y, rect.width, rect.height);
    }
}
