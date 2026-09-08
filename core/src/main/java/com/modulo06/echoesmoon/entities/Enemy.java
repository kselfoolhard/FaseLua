package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public Rectangle rect;
    public int type; // 0 = Lua (Apenas anda), 1 = Marte (Atira)
    public int hp;
    public int maxHp;
    public boolean ativo = true;
    private float speed;
    public float cooldownTiro = 0f;

    public Enemy(float x, float y, int type) {
        this.rect = new Rectangle(x, y, 36, 36);
        this.type = type;
        this.maxHp = (type == 0) ? 50 : 100;
        this.hp = this.maxHp;
        this.speed = (type == 0) ? 90f : 120f;
        this.cooldownTiro = 1f + (float) Math.random() * 2f;
    }

    public void update(float delta, Vector2 playerPos) {
        if (!ativo) return;
        Vector2 pos = new Vector2(rect.x, rect.y);
        Vector2 dir = new Vector2(playerPos).sub(pos);

        if (type == 0) {
            // LUA: Apenas persegue o jogador (sem atirar)
            if (dir.len() < 700f) {
                dir.nor();
                rect.x += dir.x * speed * delta;
                rect.y += dir.y * speed * delta;
            }
        } else if (type == 1) {
            // MARTE: Persegue até 180px e para para atirar
            if (dir.len() < 700f && dir.len() > 180f) {
                dir.nor();
                rect.x += dir.x * speed * delta;
                rect.y += dir.y * speed * delta;
            }
            if (cooldownTiro > 0) cooldownTiro -= delta;
        }
    }
}
