package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class GameOverScreen implements Screen {
    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    public GameOverScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "VOCE MORREU - FIM DE JOGO", 315, 380);
        font.draw(batch, "Pressione [M] para voltar ao Menu Principal", 270, 320);
        font.draw(batch, "Pressione [R] para carregar o último save", 270, 280);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            GameSaveData save = GameSaveData.carregar();
            if (save != null) {
                if (save.fase.equals("MARTE")) game.setScreen(new MarsScreen(game, save));
                else game.setScreen(new GameScreen(game, save));
            } else {
                game.setScreen(new MenuScreen(game));
            }
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
