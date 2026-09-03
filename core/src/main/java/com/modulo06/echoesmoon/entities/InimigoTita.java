package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public class InimigoTita {
    public final Vector2 pos = new Vector2();
    public final Rectangle bounds = new Rectangle(0, 0, 48, 48);
    public int hp = 80;
    public float speed = 70f;
    public float raioChase = 160f;

    public boolean vivo() { return hp > 0; }

    public void update(float delta, Vector2 player) {
        if (!vivo()) return;
        if (pos.dst(player) < raioChase) {
            Vector2 dir = player.cpy().sub(pos);
            if (dir.len2() > 0.001f) pos.mulAdd(dir.nor(), speed * delta);
        }
        bounds.setPosition(pos.x, pos.y);
    }
}
