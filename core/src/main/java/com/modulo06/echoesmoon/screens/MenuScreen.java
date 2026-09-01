package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MenuScreen implements Screen {
    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    private String feedbackMsg = "";
    private float feedbackTimer = 0f;

    public MenuScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void render(float delta) {
        if (feedbackTimer > 0) feedbackTimer -= delta;

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "ECHOES MOON SURVIVAL", 310, 420);
        font.draw(batch, "Pressione [1] para NOVO JOGO", 300, 350);
        font.draw(batch, "Pressione [2] para CONTINUAR (Carregar Save)", 260, 310);
        font.draw(batch, "Pressione [3] para RESETAR/APAGAR SAVE", 270, 270);

        if (feedbackTimer > 0) {
            font.draw(batch, feedbackMsg, 300, 210);
        }
        batch.end();

        // 1. Novo Jogo
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameSaveData novoSave = new GameSaveData();
            novoSave.salvar();
            game.setScreen(new GameScreen(game, novoSave));
        }

        // 2. Continuar Save
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            GameSaveData saveCarregado = GameSaveData.carregar();
            if (saveCarregado != null) {
                if (saveCarregado.fase.equals("MARTE")) {
                    game.setScreen(new MarsScreen(game, saveCarregado));
                } else {
                    game.setScreen(new GameScreen(game, saveCarregado));
                }
            } else {
                feedbackMsg = "Nenhum save encontrado!";
                feedbackTimer = 2.0f;
            }
        }

        // 3. Resetar Save
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            GameSaveData.apagar();
            feedbackMsg = "Save apagado com sucesso!";
            feedbackTimer = 2.0f;
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
