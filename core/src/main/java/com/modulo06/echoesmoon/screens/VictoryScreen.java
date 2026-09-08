package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.modulo06.echoesmoon.systems.GameSaveData;

public class VictoryScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private SpriteBatch batch;
    private BitmapFont font;

    public VictoryScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.15f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.getData().setScale(2.5f);
        font.draw(batch, "MISSÃO CUMPRIDA! VOCE VENCEU!", 300, 450);

        font.getData().setScale(1.2f);
        font.draw(batch, "A Besta de Tita foi derrotada e o setor foi purificado.", 350, 370);
        font.draw(batch, "Nivel final de O2 restante: " + (int)Math.max(0, saveData.o2) + "%", 420, 320);

        font.draw(batch, "Pressione [ESPAÇO] para jogar novamente", 400, 200);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            saveData.o2 = 100;
            saveData.municao = 25;
            saveData.fase = "LUA";
            saveData.salvar();
            game.setScreen(new GameScreen(game, saveData));
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {} @Override public void dispose() {}
}
