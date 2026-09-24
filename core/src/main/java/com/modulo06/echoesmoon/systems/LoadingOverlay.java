package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/** Globo de carregamento minimalista usado durante o fade-in das telas. */
public final class LoadingOverlay {
    private LoadingOverlay() {}

    public static void draw(ShapeRenderer shape, float time, float alpha) {
        if (alpha <= 0f) return;

        final float cx = 1215f;
        final float cy = 48f;
        final float radius = 17f;
        final int segments = 24;
        final float rotation = time * 180f;

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(1f, 1f, 1f, MathUtils.clamp(alpha, 0f, 0.9f));
        shape.circle(cx, cy, radius, segments);

        // Paralelos "curvos" simulados por elipses rotacionais.
        for (int ring = -1; ring <= 1; ring++) {
            float width = radius * (0.35f + Math.abs(ring) * 0.18f);
            drawRotatedEllipse(shape, cx, cy, width, radius, rotation + ring * 35f, 24);
        }
        drawRotatedEllipse(shape, cx, cy, radius, radius * 0.42f, rotation + 90f, 24);
        shape.end();
    }

    private static void drawRotatedEllipse(ShapeRenderer shape, float cx, float cy,
                                           float rx, float ry, float angleDeg, int segments) {
        float angle = angleDeg * MathUtils.degreesToRadians;
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);

        float px = cx + rx;
        float py = cy;
        for (int i = 1; i <= segments; i++) {
            float a = MathUtils.PI2 * i / segments;
            float x = MathUtils.cos(a) * rx;
            float y = MathUtils.sin(a) * ry;
            float xr = x * cos - y * sin;
            float yr = x * sin + y * cos;
            shape.line(px, py, cx + xr, cy + yr);
            px = cx + xr;
            py = cy + yr;
        }
    }
}
