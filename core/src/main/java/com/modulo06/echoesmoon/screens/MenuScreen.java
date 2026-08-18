package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.modulo06.echoesmoon.EchoesMoonGame;

public class MenuScreen implements Screen {
    private EchoesMoonGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    public MenuScreen(EchoesMoonGame game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "ECHOES MOON SURVIVAL", 330, 350);
        font.draw(batch, "Pressione ENTER para Começar", 310, 300);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
    }
    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {}
    @Override public void hide() {} @Override public void dispose() { batch.dispose(); font.dispose(); }
}
