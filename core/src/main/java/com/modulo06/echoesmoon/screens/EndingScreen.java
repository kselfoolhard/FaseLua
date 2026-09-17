package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import com.modulo06.echoesmoon.systems.GameSaveData;

/** Cutscene final em escala 800x600. Tambem aceita ending_*.png como plano B visual. */
public class EndingScreen implements Screen {
    private final Game game;
    private final GameSaveData save;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(800, 600, camera);
    private final Texture endingImage;
    private float time = 0f;

    public EndingScreen(Game game, GameSaveData save) {
        this.game = game; this.save = save;
        endingImage = Gdx.files.internal("ending_terra.png").exists() ? new Texture("ending_terra.png") : null;
    }

    @Override public void render(float delta) {
        time += delta;
        Gdx.gl.glClearColor(0,0,0,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (endingImage != null) drawAspectFit(endingImage);
        font.setColor(1,1,1,1);
        font.getData().setScale(1.7f);
        if (time < 2f) font.draw(batch, "TERRA", 335, 450);
        else if (time < 4f) font.draw(batch, "O conhecimento voltou com o astronauta.", 170, 400);
        else if (time < 6f) font.draw(batch, "A era nao se impoe. Ela se escolhe.", 180, 350);
        else font.draw(batch, "ECHOES — FIM", 290, 430);
        font.getData().setScale(1f);
        if (time >= 8f) font.draw(batch, "[ENTER] Voltar ao menu", 290, 220);
        batch.end();

        if (time >= 8f && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            save.fase = "LUA";
            save.salvar();
            game.setScreen(new MenuScreen(game));
        }
    }

    private void drawAspectFit(Texture texture) {
        Vector2 size = Scaling.fit.apply(texture.getWidth(), texture.getHeight(), 800, 600);
        float x = (800f - size.x) * 0.5f;
        float y = (600f - size.y) * 0.5f;
        batch.draw(texture, x, y, size.x, size.y);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); if (endingImage != null) endingImage.dispose(); }
}
