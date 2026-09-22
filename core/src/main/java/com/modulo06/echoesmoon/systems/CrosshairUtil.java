package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Esconde o cursor do sistema operacional e desenha uma mira (crosshair)
 * customizada na posicao do mouse, por cima de tudo.
 */
public final class CrosshairUtil {
    private static boolean tentouEsconder = false;
    private static OrthographicCamera telaCamera;

    private CrosshairUtil() {}

    /**
     * Troca o cursor do SO por um cursor 1x1 totalmente transparente.
     * Chame uma vez (ex.: no create() do jogo). E seguro chamar varias vezes.
     */
    public static void esconderCursorDoSistema() {
        if (tentouEsconder) return;
        tentouEsconder = true;
        Pixmap vazio = null;
        Cursor cursorInvisivel = null;
        try {
            vazio = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            vazio.setColor(0f, 0f, 0f, 0f);
            vazio.fill();
            cursorInvisivel = Gdx.graphics.newCursor(vazio, 0, 0);
            Gdx.graphics.setCursor(cursorInvisivel);
        } catch (Exception ignored) {
            // Alguns backends podem nao suportar cursor customizado; falha silenciosa.
        } finally {
            if (vazio != null) vazio.dispose();
        }
    }

    /** Desenha uma mira simples na posicao atual do mouse, em coordenadas de tela (pixels). */
    public static void desenharMira(ShapeRenderer shape) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        if (telaCamera == null) telaCamera = new OrthographicCamera();
        telaCamera.setToOrtho(false, w, h);
        telaCamera.update();

        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();
        float pontaExterna = 9f;
        float pontaInterna = 3f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(telaCamera.combined);

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(1f, 1f, 1f, 0.9f);
        shape.line(mx - pontaExterna, my, mx - pontaInterna, my);
        shape.line(mx + pontaInterna, my, mx + pontaExterna, my);
        shape.line(mx, my - pontaExterna, mx, my - pontaInterna);
        shape.line(mx, my + pontaInterna, mx, my + pontaExterna);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(1f, 0.25f, 0.2f, 0.95f);
        shape.circle(mx, my, 1.6f);
        shape.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
