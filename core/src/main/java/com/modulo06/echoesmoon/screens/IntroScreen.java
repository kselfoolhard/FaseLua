package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.modulo06.echoesmoon.systems.GameSaveData;

public class IntroScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private Texture[] slides;
    private String[] legendas;
    private int slideAtual = 0;

    private float fadeAlpha = 1.0f;
    private boolean fadingIn = true;
    private boolean fadingOut = false;

    public IntroScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.4f);
        shapeRenderer = new ShapeRenderer();

        // Imagens de fundo
        slides = new Texture[]{
            carregarTexture("intro_1.png"),
            carregarTexture("intro_2.png"),
            carregarTexture("intro_3.png")
        };

        // Textos
        legendas = new String[]{
            "Ano 2142.",
            "A Estacao Lunar Echoes perdeu a comunicacao...",
            "Sistemas de suporte a vida colapsaram.",
            "O oxigenio esta terminando.",
            "Sua missao: Investigar a base, e sobreviver."
        };
    }

    private Texture carregarTexture(String path) {
        return Gdx.files.internal(path).exists() ? new Texture(path) : null;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (fadingIn) {
            fadeAlpha = Math.max(0f, fadeAlpha - delta * 2f);
            if (fadeAlpha <= 0) fadingIn = false;
        } else if (fadingOut) {
            fadeAlpha = Math.min(1f, fadeAlpha + delta * 2f);
            if (fadeAlpha >= 1f) {
                // Vai para a tela de jogo principal
                game.setScreen(new GameScreen(game, saveData));
                return;
            }
        }

        if (!fadingIn && !fadingOut) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

                if (slideAtual < slides.length - 1) {
                    slideAtual++;
                    fadingIn = true;
                    fadeAlpha = 1.0f;
                } else {
                    fadingOut = true;
                }
            }
        }

        batch.begin();
        if (slides[slideAtual] != null) {
            batch.draw(slides[slideAtual], 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        font.setColor(1, 1, 1, 1);
        font.draw(batch, legendas[slideAtual], 80, 100);
        font.setColor(0.6f, 0.6f, 0.6f, 1);
        font.draw(batch, "[Pressione ENTER ou Clique para Avancar]", 80, 50);
        batch.end();

        if (fadeAlpha > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, fadeAlpha);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override public void dispose() {
        batch.dispose(); font.dispose(); shapeRenderer.dispose();
        for (Texture t : slides) if (t != null) t.dispose();
    }
}
