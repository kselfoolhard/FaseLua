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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.RouteSystem;

/** Aharin: 3 falas, verifica a rota estranha e dispara o ending correspondente. */
public class AharinScreen implements Screen {
    private final Game game;
    private final GameSaveData save;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final DialogSystem dialog = new DialogSystem();
    private final Texture fundo;
    private final com.badlogic.gdx.graphics.OrthographicCamera camera = new com.badlogic.gdx.graphics.OrthographicCamera(800, 600);

    private boolean endingStarted = false;
    private boolean routeChecked = false;
    private boolean routeActivatedNow = false;
    private float routeMessageTimer = 0f;

    public AharinScreen(Game game, GameSaveData save) {
        this.game = game;
        this.save = save;
        fundo = Gdx.files.internal("fundo_aharin.png").exists()
                ? new Texture("fundo_aharin.png") : null;

        dialog.start(new String[]{
                "Voce atravessou quatro mundos por uma pergunta, nao por uma arma.",
                "A Luz nao se guarda: ela se entrega a quem nao destroi.",
                "Volte. A Terra ainda pode escolher."
        }, null);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.72f, 0.86f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!routeChecked) {
            routeChecked = true;
            routeActivatedNow = RouteSystem.tryActivate(save);
            save.fase = "AHARIN";
            save.salvar();
            if (routeActivatedNow) routeMessageTimer = 4f;
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (fundo != null) drawAspectFit(fundo);
        font.setColor(Color.NAVY);
        font.getData().setScale(2f);
        font.draw(batch, "AHARIN", 50, 650);
        font.getData().setScale(1f);
        font.draw(batch, "Planeta das Entidades de Luz", 50, 610);
        batch.end();

        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialog.next();
            }
            dialog.render(batch, shape, font);
            return;
        }

        if (routeMessageTimer > 0f) {
            routeMessageTimer -= delta;
            drawRouteMessage();
            return;
        }

        if (!endingStarted) {
            endingStarted = true;
            if (save.rotaEstranha) {
                game.setScreen(new VideoCutsceneScreen(
                        game, "video/weirdending.mp4", new MenuScreen(game), false));
            } else {
                game.setScreen(new VideoCutsceneScreen(
                        game, "video/goodending.webm", new MenuScreen(game), false));
            }
        }
    }

    private void drawRouteMessage() {
        batch.begin();
        font.setColor(Color.RED);
        font.getData().setScale(2.1f);
        font.draw(batch, "ROTA ESTRANHA ATIVADA!", 190, 360);
        font.setColor(Color.WHITE);
        font.getData().setScale(1.15f);
        font.draw(batch, "Voce ficou forte demais para seguir o destino normal.", 135, 320);
        font.getData().setScale(1f);
        batch.end();
    }

    private void drawAspectFit(Texture texture) {
        Vector2 size = Scaling.fit.apply(texture.getWidth(), texture.getHeight(), 800f, 600f);
        batch.draw(texture, (800f - size.x) / 2f, (600f - size.y) / 2f, size.x, size.y);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        font.dispose();
        shape.dispose();
        if (fundo != null) fundo.dispose();
    }
}
