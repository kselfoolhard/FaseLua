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
import com.badlogic.gdx.utils.Scaling;

import java.lang.reflect.Method;

/** Reprodutor de video gdx-video para desktop/LWJGL3. */
public class VideoCutsceneScreen implements Screen {
    private Object videoPlayer;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont debugFont = new BitmapFont();
    private final Game game;
    private final Screen nextScreen;
    private Texture fallbackImage;

    private boolean started = false;
    private boolean sawFrame = false;
    private float waitTimer = 0f;
    private float finishedTimer = 0f;
    private String errorMessage = null;

    public VideoCutsceneScreen(Game game, String videoPath, String fallbackImagePath, Screen nextScreen) {
        this.game = game;
        this.nextScreen = nextScreen;

        if (fallbackImagePath != null && Gdx.files.internal(fallbackImagePath).exists()) {
            fallbackImage = new Texture(Gdx.files.internal(fallbackImagePath));
        }

        try {
            if (!Gdx.files.internal(videoPath).exists()) {
                throw new IllegalStateException("Arquivo nao encontrado: " + videoPath);
            }

            Class<?> creator = Class.forName("com.badlogic.gdx.video.VideoPlayerCreator");
            videoPlayer = creator.getMethod("createVideoPlayer").invoke(null);

            Method load = videoPlayer.getClass().getMethod("load", com.badlogic.gdx.files.FileHandle.class);
            Object result = load.invoke(videoPlayer, Gdx.files.internal(videoPath));

            if (result instanceof Boolean && !((Boolean) result)) {
                throw new IllegalStateException("gdx-video recusou o arquivo: " + videoPath);
            }

            // IMPORTANTE: nao esperar isBuffered() para tocar.
            // A API oficial permite load() e depois play(); update() deve ser chamado a cada frame.
            videoPlayer.getClass().getMethod("play").invoke(videoPlayer);
            started = true;

        } catch (Throwable t) {
            errorMessage = "VIDEO ERROR: " + t.getClass().getSimpleName();
            Gdx.app.error("VIDEO_SYSTEM", "Falha ao abrir " + videoPath, t);
            videoPlayer = null;
        }
    }

    public VideoCutsceneScreen(Game game, String videoPath, Screen nextScreen) {
        this(game, videoPath, null, nextScreen);
    }

    @Override
    public void render(float delta) {
        waitTimer += delta;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            encerrar();
            return;
        }

        batch.begin();

        if (videoPlayer != null) {
            try {
                // O decoder precisa ser atualizado em TODO frame.
                videoPlayer.getClass().getMethod("update").invoke(videoPlayer);

                Texture frame = (Texture) videoPlayer.getClass().getMethod("getTexture").invoke(videoPlayer);

                if (frame != null && frame.getWidth() > 1 && frame.getHeight() > 1) {
                    sawFrame = true;

                    // Usa o tamanho do frame para evitar depender de isBuffered().
                    Scaling.fit.apply(
                            frame.getWidth(),
                            frame.getHeight(),
                            Gdx.graphics.getWidth(),
                            Gdx.graphics.getHeight()
                    );

                    float scale = Math.min(
                            (float) Gdx.graphics.getWidth() / frame.getWidth(),
                            (float) Gdx.graphics.getHeight() / frame.getHeight()
                    );
                    float width = frame.getWidth() * scale;
                    float height = frame.getHeight() * scale;
                    float x = (Gdx.graphics.getWidth() - width) * 0.5f;
                    float y = (Gdx.graphics.getHeight() - height) * 0.5f;

                    batch.draw(frame, x, y, width, height);
                } else if (fallbackImage != null) {
                    batch.draw(fallbackImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }

                if (started) {
                    boolean playing = (Boolean) videoPlayer.getClass().getMethod("isPlaying").invoke(videoPlayer);
                    if (!playing && sawFrame) {
                        finishedTimer += delta;
                    } else {
                        finishedTimer = 0f;
                    }
                }

            } catch (Throwable t) {
                errorMessage = "VIDEO ERROR: " + t.getClass().getSimpleName();
                Gdx.app.error("VIDEO_SYSTEM", "Falha ao atualizar o video", t);

                if (fallbackImage != null) {
                    batch.draw(fallbackImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            }
        } else if (fallbackImage != null) {
            batch.draw(fallbackImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Diagnostico visivel quando o player/decoder falhar.
        if (errorMessage != null) {
            debugFont.setColor(Color.WHITE);
            debugFont.draw(batch, errorMessage, 20, 40);
        } else if (!sawFrame && waitTimer > 5f) {
            debugFont.setColor(Color.WHITE);
            debugFont.draw(batch, "VIDEO: decoder carregado, mas nenhum frame foi recebido", 20, 40);
        }

        batch.end();

        if (started && finishedTimer > 0.25f) {
            encerrar();
        }
    }

    private void encerrar() {
        if (videoPlayer != null) {
            try { videoPlayer.getClass().getMethod("stop").invoke(videoPlayer); } catch (Throwable ignored) {}
            try { videoPlayer.getClass().getMethod("dispose").invoke(videoPlayer); } catch (Throwable ignored) {}
            videoPlayer = null;
        }

        if (game != null && nextScreen != null) {
            game.setScreen(nextScreen);
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (videoPlayer != null) {
            try { videoPlayer.getClass().getMethod("dispose").invoke(videoPlayer); } catch (Throwable ignored) {}
        }
        if (fallbackImage != null) {
            fallbackImage.dispose();
            fallbackImage = null;
        }
        debugFont.dispose();
        batch.dispose();
    }
}
