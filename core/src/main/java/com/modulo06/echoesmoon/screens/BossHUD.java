package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.modulo06.echoesmoon.systems.GameSaveData;

/** HUD fixa para arenas de boss, usando a mesma base 1280x720 da campanha. */
public final class BossHUD {
    private BossHUD() {}

    public static void render(SpriteBatch batch, ShapeRenderer shape, BitmapFont font,
                              GameSaveData save, String bossName, int hp, int maxHp) {
        Matrix4 hud = new Matrix4().setToOrtho2D(0, 0, 1280, 720);
        batch.setProjectionMatrix(hud);
        shape.setProjectionMatrix(hud);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.04f, 0.04f, 0.05f, 0.82f);
        shape.rect(20, 20, 300, 116);

        shape.setColor(0.12f, 0.12f, 0.14f, 1f);
        shape.rect(390, 680, 500, 18);
        shape.setColor(0.75f, 0.12f, 0.12f, 1f);
        shape.rect(390, 680, 500 * Math.max(0f, Math.min(1f, hp / (float) maxHp)), 18);
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, bossName, 440, 715);
        font.draw(batch, "HP DO CHEFE: " + Math.max(0, hp) + " / " + maxHp, 520, 665);
        font.draw(batch, "O2: " + (int) Math.max(0, save.o2), 35, 112);
        font.draw(batch, "ARMADURA NV." + save.inventario.nivelArmadura, 35, 85);
        font.draw(batch, "ABATES: " + save.inimigosDerrotados, 35, 58);
        font.draw(batch, "[I] INVENTARIO   [WASD] MOVER", 35, 30);
        batch.end();
    }
}
