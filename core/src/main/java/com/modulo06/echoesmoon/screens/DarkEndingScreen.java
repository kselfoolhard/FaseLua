package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.modulo06.echoesmoon.systems.GameSaveData;

/** Final alternativo: rota estranha. */
public class DarkEndingScreen implements Screen {
    private final Game game;
    private final GameSaveData save;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(800, 600, camera);
    private final Texture endingDarkImage;

    private float time = 0f;

    public DarkEndingScreen(Game game, GameSaveData save) {
        this.game = game;
        this.save = save;
        endingDarkImage = Gdx.files.internal("ending_dark.png").exists()
                ? new Texture("ending_dark.png") : null;
    }

    @Override
    public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0.015f, 0.008f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.position.set(400f, 300f, 0f);
        camera.update();

        if (time >= 2f && time < 4.6f) {
            camera.position.x += MathUtils.random(-4f, 4f);
            camera.position.y += MathUtils.random(-4f, 4f);
            camera.update();
        }

        if (endingDarkImage != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            drawAspectFit(endingDarkImage);
            batch.end();
        }

        drawSlashSequence();
        drawText();

        if (time >= 8f && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            save.fase = "LUA";
            save.salvar();
            game.setScreen(new MenuScreen(game));
        }
    }

    private void drawSlashSequence() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        if (time < 5.5f) {
            shape.setColor(new Color(0.10f, 0.12f, 0.16f, 1f));
            shape.circle(400f, 335f, 52f);
            shape.rect(360f, 230f, 80f, 100f);
        }

        if (time >= 2f && time < 4.7f) {
            float local = time - 2f;
            int slashCount = Math.min(24, (int) (local * 11f));
            for (int i = 0; i < slashCount; i++) {
                float phase = i * 0.73f + local * 9f;
                float x = 250f + (i % 6) * 60f + MathUtils.sin(phase) * 32f;
                float y = 220f + ((i * 73) % 220) + MathUtils.cos(phase * 1.3f) * 20f;
                float len = 60f + (i % 4) * 18f;
                float angleDeg = -35f + (i % 5) * 12f;
                float angle = angleDeg * MathUtils.degreesToRadians;
                float x2 = x + MathUtils.cos(angle) * len;
                float y2 = y + MathUtils.sin(angle) * len;

                shape.setColor(i % 3 == 0
                        ? Color.WHITE
                        : new Color(1f, 0.15f, 0.25f, 1f));

                // ShapeRenderer de algumas versoes do libGDX nao possui
                // o overload rect(x,y,w,h,originX,originY,rotation).
                // Desenhar uma linha evita a incompatibilidade de versao.
                shape.rectLine(x, y, x2, y2, 5f);
            }
        }

        if (time >= 4.7f && time < 5f) {
            float alpha = 1f - (time - 4.7f) / 0.3f;
            shape.setColor(new Color(1f, 1f, 1f, alpha));
            shape.rect(0f, 0f, 800f, 600f);
        }
        shape.end();
    }

    private void drawText() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.getData().setScale(1.5f);

        if (time < 1.8f) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "VOCE TROUXE FORCA DEMAIS.", 205f, 490f);
        } else if (time < 5.2f) {
            font.setColor(Color.WHITE);
            font.draw(batch, "...", 397f, 170f);
        } else if (time < 6.8f) {
            font.setColor(new Color(1f, 0.22f, 0.3f, 1f));
            font.draw(batch, "A LUZ NAO FOI LEVADA A TERRA.", 165f, 435f);
            font.draw(batch, "ELA FOI SUBMETIDA.", 245f, 385f);
        } else if (time < 8f) {
            font.setColor(Color.WHITE);
            font.draw(batch, "A TERRA AGORA TEM UM SOBERANO.", 180f, 330f);
            font.draw(batch, "ECHOES - ROTA ESTRANHA", 230f, 255f);
        } else {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "FINAL ALTERNATIVO", 275f, 420f);
            font.getData().setScale(1f);
            font.draw(batch, "[ENTER] Voltar ao menu", 310f, 220f);
        }

        font.getData().setScale(1f);
        batch.end();
    }

    private void drawAspectFit(Texture texture) {
        Vector2 size = Scaling.fit.apply(texture.getWidth(), texture.getHeight(), 800f, 600f);
        float x = (800f - size.x) * 0.5f;
        float y = (600f - size.y) * 0.5f;
        batch.draw(texture, x, y, size.x, size.y);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
        if (endingDarkImage != null) endingDarkImage.dispose();
    }
}
