package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.modulo06.echoesmoon.systems.GameSaveData;

public class MenuScreen implements Screen {
    private final Game game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private String feedbackMsg = "";
    private float feedbackTimer = 0f;

    public MenuScreen(Game game) {
        this.game = game;
        camera.setToOrtho(false, 800, 600);
    }

    @Override public void render(float delta) {
        if (feedbackTimer > 0) feedbackTimer -= delta;
        Gdx.gl.glClearColor(0.1f,0.1f,0.15f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update(); batch.setProjectionMatrix(camera.combined); batch.begin();
        font.draw(batch, "ECHOES MOON SURVIVAL", 310, 420);
        font.draw(batch, "Pressione [1] para NOVO JOGO", 300, 350);
        font.draw(batch, "Pressione [2] para CONTINUAR", 300, 310);
        font.draw(batch, "Pressione [3] para RESETAR SAVE", 300, 270);
        if (feedbackTimer > 0) font.draw(batch, feedbackMsg, 300, 210);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameSaveData novo = new GameSaveData();
            novo.salvar();
            game.setScreen(new GameScreen(game, novo));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            GameSaveData save = GameSaveData.carregar();
            if (save == null) {
                feedbackMsg = "Nenhum save encontrado!";
                feedbackTimer = 2f;
            } else {
                save.sincronizarInventario();
                switch (save.fase) {
                    case "LUA_BOSS": game.setScreen(new BossLuaScreen(game, save)); break;
                    case "MARTE": game.setScreen(new MarsScreen(game, save)); break;
                    case "MARTE_BOSS": game.setScreen(new BossMarteScreen(game, save)); break;
                    case "TITA": game.setScreen(new TitanScreen(game, save)); break;
                    case "CALISTO": game.setScreen(new CallistoScreen(game, save)); break;
                    case "AHARIN": game.setScreen(new AharinScreen(game, save)); break;
                    default: game.setScreen(new GameScreen(game, save)); break;
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            GameSaveData.apagar();
            feedbackMsg = "Save apagado com sucesso!";
            feedbackTimer = 2f;
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
