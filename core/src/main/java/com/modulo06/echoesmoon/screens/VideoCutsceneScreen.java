package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.lang.reflect.Method;

/**
 * Tela de video com fallback. Usa gdx-video por reflexao para o projeto
 * continuar compilando mesmo antes da dependencia de video ser adicionada.
 * Escala em uma viewport 800x600, sem esticar a proporcao do video.
 */
public class VideoCutsceneScreen implements Screen {
    private final Game game;
    private final String assetPath;
    private final Screen nextScreen;
    private final boolean allowSkip;

    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Viewport viewport = new FitViewport(800f, 600f);

    private Object videoPlayer;
    private Method updateMethod;
    private Method getTextureMethod;
    private Method disposeMethod;
    private Method isFinishedMethod;
    private Texture fallbackTexture;
    private float fallbackTimer = 0f;
    private boolean videoReady = false;
    private float safetyTimer = 0f;
    // Trava de seguranca: se o player nao tiver como detectar o fim (isFinished ausente
    // em algumas implementacoes do gdx-video), nao deixamos o jogador preso na cutscene.
    private static final float MAX_DURACAO_SEM_DETECCAO = 90f;

    public VideoCutsceneScreen(Game game, String assetPath, Screen nextScreen, boolean allowSkip) {
        this.game = game;
        this.assetPath = assetPath;
        this.nextScreen = nextScreen;
        this.allowSkip = allowSkip;
    }

    @Override
    public void show() {
        tryStartVideo();
    }

    private void tryStartVideo() {
        if (!Gdx.files.internal(assetPath).exists()) return;

        try {
            Class<?> creator = Class.forName("com.badlogic.gdx.video.VideoPlayerCreator");
            Method create = creator.getMethod("createVideoPlayer");
            videoPlayer = create.invoke(null);

            Class<?> playerClass = videoPlayer.getClass();
            updateMethod = playerClass.getMethod("update");
            getTextureMethod = playerClass.getMethod("getTexture");
            disposeMethod = playerClass.getMethod("dispose");

            try { isFinishedMethod = playerClass.getMethod("isFinished"); }
            catch (NoSuchMethodException ignored) { isFinishedMethod = null; }

            // A API do gdx-video nao tem um metodo "load" separado: o video e aberto
            // e comeca a tocar direto com play(FileHandle). Chamar um "load" (que nao
            // existe na interface VideoPlayer) sempre derrubava a cutscene antes,
            // caindo silenciosamente no fallback de texto.
            playerClass.getMethod("play", com.badlogic.gdx.files.FileHandle.class)
                    .invoke(videoPlayer, Gdx.files.internal(assetPath));

            videoReady = true;
            safetyTimer = 0f;
        } catch (Throwable ignored) {
            videoReady = false;
            disposeVideo();
        }
    }

    @Override
    public void render(float delta) {
        fallbackTimer += delta;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(true);
        Texture frame = null;

        if (videoReady) {
            try {
                updateMethod.invoke(videoPlayer);
                Object texture = getTextureMethod.invoke(videoPlayer);
                if (texture instanceof Texture) frame = (Texture) texture;

                if (isFinishedMethod != null) {
                    if (Boolean.TRUE.equals(isFinishedMethod.invoke(videoPlayer))) {
                        finish();
                        return;
                    }
                } else {
                    // Sem deteccao de fim disponivel: usa uma trava de tempo maxima para
                    // nunca deixar o jogador preso numa cutscene sem saida.
                    safetyTimer += delta;
                    if (safetyTimer >= MAX_DURACAO_SEM_DETECCAO) {
                        finish();
                        return;
                    }
                }
            } catch (Throwable ignored) {
                videoReady = false;
            }
        }

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (frame != null) {
            Vector2 size = Scaling.fit.apply(frame.getWidth(), frame.getHeight(), 800f, 600f);
            float x = (800f - size.x) * 0.5f;
            float y = (600f - size.y) * 0.5f;
            batch.draw(frame, x, y, size.x, size.y);
        } else {
            font.setColor(Color.WHITE);
            font.draw(batch, Gdx.files.internal(assetPath).exists()
                    ? "VIDEO NAO DISPONIVEL — FALLBACK"
                    : "ARQUIVO DE VIDEO AUSENTE", 235, 330);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, assetPath, 315, 295);

            if (fallbackTimer >= 3f || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                finish();
            }
        }

        if (allowSkip && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            finish();
        }
        // Saida de emergencia: ESC sempre funciona, mesmo em cutscenes sem skip,
        // para nunca travar o jogador numa tela de video.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            finish();
        }
        batch.end();
    }

    private void finish() {
        disposeVideo();
        if (nextScreen != null) game.setScreen(nextScreen);
    }

    private void disposeVideo() {
        if (videoPlayer != null && disposeMethod != null) {
            try { disposeMethod.invoke(videoPlayer); } catch (Throwable ignored) {}
        }
        videoPlayer = null;
        videoReady = false;
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { disposeVideo(); }
    @Override public void dispose() { disposeVideo(); batch.dispose(); font.dispose(); if (fallbackTexture != null) fallbackTexture.dispose(); }
}
