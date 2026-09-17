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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import com.modulo06.echoesmoon.systems.GameSaveData;

/** Intro em viewport virtual 800x600 para nao esticar as artes. */
public class IntroScreen implements Screen {
    private final Game game;
    private final GameSaveData saveData;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(800, 600, camera);
    private final Texture[] slides = new Texture[3];
    private final String[] legendas = {
        "Ano 2142.",
        "A Estacao Lunar Echoes perdeu a comunicacao...",
        "Sistemas de suporte a vida colapsaram. O oxigenio esta terminando."
    };
    private int slideAtual = 0;
    private float tempo = 0f;
    private float fadeAlpha = 1f;
    private boolean fading = false;

    public IntroScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        slides[0] = load("intro_1.png");
        slides[1] = load("intro_2.png");
        slides[2] = load("intro_3.png");
    }

    private Texture load(String path) { return Gdx.files.internal(path).exists() ? new Texture(path) : null; }

    @Override public void render(float delta) {
        tempo += delta;
        if (!fading && slideAtual < slides.length - 1 && tempo >= 2f) {
            slideAtual++;
            tempo = 0f;
        }
        if (fading) {
            fadeAlpha = Math.min(1f, fadeAlpha + delta * 2f);
            if (fadeAlpha >= 1f) { game.setScreen(new MenuScreen(game)); return; }
        } else {
            fadeAlpha = Math.max(0f, fadeAlpha - delta * 2f);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (slides[slideAtual] != null) drawAspectFit(slides[slideAtual]);
        font.setColor(1,1,1,1); font.getData().setScale(1.25f);
        font.draw(batch, legendas[slideAtual], 45, 90);
        font.getData().setScale(1f);
        font.draw(batch, tempoTotalHint(), 45, 45);
        batch.end();

        if (fadeAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shape.setProjectionMatrix(camera.combined);
            shape.begin(ShapeRenderer.ShapeType.Filled); shape.setColor(0,0,0,fadeAlpha); shape.rect(0,0,800,600); shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // So permite pular depois de seis segundos reais, como pede o guia.
        if (!fading && getElapsedTotal() >= 6f && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            fading = true;
            fadeAlpha = 0f;
        }
    }

    private float getElapsedTotal() { return slideAtual * 2f + tempo; }
    private String tempoTotalHint() { return getElapsedTotal() < 6f ? "Intro: aguarde 6 segundos..." : "[ENTER] / [SPACE] continuar"; }

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
    @Override public void dispose() { batch.dispose(); font.dispose(); shape.dispose(); for (Texture t : slides) if (t != null) t.dispose(); }
}
