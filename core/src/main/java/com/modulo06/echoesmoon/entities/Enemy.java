package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public Rectangle rect;
    public int type; // 0 = lunar, 1 = chase
    public int hp;
    public int maxHp;
    public boolean ativo = true;
    private float speed;

    public Enemy(float x, float y, int type) {
        this.rect = new Rectangle(x, y, 36, 36);
        this.type = type;
        this.maxHp = (type == 0) ? 50 : 100;
        this.hp = this.maxHp;
        this.speed = (type == 0) ? 90f : 140f;
    }

    public void update(float delta, Vector2 playerPos) {
        if (!ativo) return;
        Vector2 pos = new Vector2(rect.x, rect.y);
        Vector2 dir = new Vector2(playerPos).sub(pos);
        if (dir.len() < 700f) { // Raio de visão
            dir.nor();
            rect.x += dir.x * speed * delta;
            rect.y += dir.y * speed * delta;
        }
    }
}
