package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;

/** Ataque corpo-a-corpo usado enquanto o astronauta ainda esta sem arma. */
public final class PlayerCombat {
    private PlayerCombat() {}

    public static Rectangle punchBox(Rectangle player, Vector3 target) {
        float cx = player.x + player.width * 0.5f;
        float cy = player.y + player.height * 0.5f;
        Vector2 dir = new Vector2(target.x - cx, target.y - cy);
        if (dir.isZero()) dir.set(1f, 0f);
        dir.nor();
        float reach = 46f;
        float size = 34f;
        float centerX = cx + dir.x * reach;
        float centerY = cy + dir.y * reach;
        return new Rectangle(centerX - size * 0.5f, centerY - size * 0.5f, size, size);
    }

    public static Rectangle punchBox(Rectangle player, float targetX, float targetY) {
        return punchBox(player, new Vector3(targetX, targetY, 0f));
    }

    /** Desenha particulas simples ao redor do astronauta durante o carregamento. */
    public static void drawChargeParticles(ShapeRenderer shape, Rectangle player, float chargeTime, float stateTime) {
        if (chargeTime <= 0f) return;
        float ratio = Math.min(1f, chargeTime / 0.8f);
        float cx = player.x + player.width * 0.5f;
        float cy = player.y + player.height * 0.5f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 10; i++) {
            float a = stateTime * (120f + i * 9f) + i * 36f;
            float radius = 22f + 12f * ratio + (float)Math.sin(stateTime * 7f + i) * 3f;
            float x = cx + (float)Math.cos(Math.toRadians(a)) * radius;
            float y = cy + (float)Math.sin(Math.toRadians(a)) * radius;
            shape.setColor(0.55f, 0.85f, 1f, 0.28f + 0.55f * ratio);
            shape.circle(x, y, 1.5f + ratio * 1.8f);
        }
        shape.setColor(Color.WHITE);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

}
