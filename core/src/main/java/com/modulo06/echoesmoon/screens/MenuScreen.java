package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.modulo06.echoesmoon.systems.GameSaveData;

/** Menu principal com wallpaper animado e botoes desenhados. */
public class MenuScreen implements Screen {
    private final Game game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final OrthographicCamera camera = new OrthographicCamera();

    private final Rectangle[] buttons = {
        new Rectangle(90, 280, 300, 58),
        new Rectangle(90, 210, 300, 58),
        new Rectangle(90, 140, 300, 58),
        new Rectangle(90, 70, 300, 58),
        new Rectangle(460, 70, 170, 42),
        new Rectangle(650, 70, 170, 42),
        new Rectangle(840, 70, 170, 42)
    };

    private Texture background, logo, astronautTex, border;
    private Animation<TextureRegion> astronaut;
    private float animTime = 0f;
    private String feedbackMsg = "";
    private float feedbackTimer = 0f;

    public MenuScreen(Game game) {
        this.game = game;

        camera.setToOrtho(false, 1280, 720);

        background = safeLoad("menu_background.png");
        logo = safeLoad("logo.png");
        astronautTex = safeLoad("menu_astronaut.png");
        border = safeLoad("border.png");

        if (astronautTex != null) {
            int frameW = Math.max(1, astronautTex.getWidth() / 4);
            TextureRegion[][] split = TextureRegion.split(
                astronautTex,
                frameW,
                astronautTex.getHeight()
            );

            astronaut = new Animation<>(0.16f, split[0]);
            astronaut.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    private Texture safeLoad(String path) {
        try {
            return Gdx.files.internal(path).exists() ? new Texture(path) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void render(float delta) {
        animTime += delta;

        if (feedbackTimer > 0) {
            feedbackTimer -= delta;
        }

        Gdx.gl.glClearColor(0.02f, 0.03f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Fundo e elementos principais
        batch.begin();

        if (background != null) {
            batch.draw(background, 0, 0, 1280, 720);
        }

        if (logo != null) {
            batch.draw(logo, 72, 440, 560, 180);
        } else {
            font.setColor(Color.WHITE);
            font.getData().setScale(2.2f);
            font.draw(batch, "ECHOES MOON", 88, 575);
            font.getData().setScale(1f);
        }

        if (astronaut != null) {
            batch.draw(
                astronaut.getKeyFrame(animTime),
                700, 225,
                360, 360
            );
        }

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.9f);
        font.draw(batch, "SURVIVAL // 2142", 92, 418);
        font.getData().setScale(1f);

        if (feedbackTimer > 0) {
            font.draw(batch, feedbackMsg, 90, 45);
        }

        batch.end();

        // Botoes
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        drawButton(buttons[0], false);
        drawButton(buttons[1], false);
        drawButton(buttons[2], false);
        drawButton(buttons[3], true);
        drawButton(buttons[4], false);
        drawButton(buttons[5], false);
        drawButton(buttons[6], false);

        shape.end();

        // Textos dos botoes
        batch.begin();

        drawButtonText(buttons[0], "NOVO JOGO");
        drawButtonText(buttons[1], "CONTINUAR 1");
        drawButtonText(buttons[2], "CONTINUAR 2");
        drawButtonText(buttons[3], "RESETAR SAVES");

        drawButtonText(buttons[4], "FACIL");
        drawButtonText(buttons[5], "NORMAL");
        drawButtonText(buttons[6], "DIFICIL");

        font.setColor(Color.WHITE);
        font.getData().setScale(0.8f);
        font.draw(batch, "DIFICULDADE", 460, 128);

        font.setColor(Color.GRAY);
        font.draw(
            batch,
            "Mouse: clique nos botoes  |  Teclado: 1/2/4/3 e 5/6/7",
            90,
            26
        );

        font.getData().setScale(1f);

        if (border != null) {
            batch.draw(border, 0, 0, 1280, 720);
        }

        batch.end();

        handleInput();
    }

    private void drawButton(Rectangle r, boolean danger) {
        float mouseX = 1280f * Gdx.input.getX()
            / Math.max(1f, (float) Gdx.graphics.getWidth());

        float mouseY = 720f * (1f - Gdx.input.getY()
            / Math.max(1f, (float) Gdx.graphics.getHeight()));

        boolean hovered = r.contains(mouseX, mouseY);

        shape.setColor(
            danger
                ? (hovered
                    ? new Color(0.52f, 0.10f, 0.16f, 0.98f)
                    : new Color(0.30f, 0.07f, 0.10f, 0.94f))
                : (hovered
                    ? new Color(0.10f, 0.28f, 0.40f, 0.99f)
                    : new Color(0.05f, 0.12f, 0.20f, 0.96f))
        );

        shape.rect(r.x, r.y, r.width, r.height);

        // Borda
        shape.setColor(
            hovered
                ? new Color(0.55f, 0.90f, 1f, 1f)
                : new Color(0.22f, 0.65f, 0.86f, 0.9f)
        );

        shape.rect(r.x, r.y + r.height - 3, r.width, 3);
        shape.rect(r.x, r.y, 3, r.height);
        shape.rect(r.x + r.width - 3, r.y, 3, r.height);

        // Glow
        if (hovered) {
            shape.setColor(new Color(0.35f, 0.85f, 1f, 0.18f));
            shape.rect(
                r.x + 3,
                r.y + 3,
                r.width - 6,
                r.height - 6
            );
        }
    }

    private void drawButtonText(Rectangle r, String text) {
        font.setColor(Color.WHITE);
        font.getData().setScale(r.width < 200 ? 0.82f : 0.92f);

        // Corrigido: usa GlyphLayout em vez de getBounds().
        layout.setText(font, text);

        float x = r.x + (r.width - layout.width) / 2f;
        float y = r.y + (r.height + layout.height) / 2f;

        font.draw(batch, text, x, y);

        font.getData().setScale(1f);
    }

    private void handleInput() {
        // Teclado
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startNew(1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            continueSlot(1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            continueSlot(2);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            GameSaveData.apagar();
            feedbackMsg = "SAVES RESETADOS";
            feedbackTimer = 2f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            setDifficulty("FACIL");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            setDifficulty("NORMAL");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
            setDifficulty("DIFICIL");
        }

        // Mouse
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float x = 1280f * Gdx.input.getX()
                / Math.max(1f, (float) Gdx.graphics.getWidth());

            float y = 720f * (1f - Gdx.input.getY()
                / Math.max(1f, (float) Gdx.graphics.getHeight()));

            if (buttons[0].contains(x, y)) {
                startNew(1);
            } else if (buttons[1].contains(x, y)) {
                continueSlot(1);
            } else if (buttons[2].contains(x, y)) {
                continueSlot(2);
            } else if (buttons[3].contains(x, y)) {
                GameSaveData.apagar();
                feedbackMsg = "SAVES RESETADOS";
                feedbackTimer = 2f;
            } else if (buttons[4].contains(x, y)) {
                setDifficulty("FACIL");
            } else if (buttons[5].contains(x, y)) {
                setDifficulty("NORMAL");
            } else if (buttons[6].contains(x, y)) {
                setDifficulty("DIFICIL");
            }
        }
    }

    private void setDifficulty(String dificuldade) {
        GameSaveData save = GameSaveData.carregar(1);

        if (save == null) {
            save = new GameSaveData();
        }

        save.dificuldade = dificuldade;
        save.salvar(1);

        feedbackMsg = "DIFICULDADE: " + dificuldade;
        feedbackTimer = 2f;
    }

    private void startNew(int slot) {
        GameSaveData novo = new GameSaveData();
        novo.slotId = slot;
        novo.salvar(slot);

        game.setScreen(new GameScreen(game, novo));
    }

    private void continueSlot(int slot) {
        GameSaveData save = GameSaveData.carregar(slot);

        if (save == null) {
            feedbackMsg = "NENHUM SAVE ENCONTRADO";
            feedbackTimer = 2f;
            return;
        }

        save.sincronizarInventario();

        switch (save.fase) {
            case "LUA_BOSS":
                game.setScreen(new BossLuaScreen(game, save));
                break;

            case "MARTE":
                game.setScreen(new MarsScreen(game, save));
                break;

            case "MARTE_BOSS":
                game.setScreen(new BossMarteScreen(game, save));
                break;

            case "TITA":
                game.setScreen(new TitanScreen(game, save));
                break;

            case "CALISTO":
                game.setScreen(new CallistoScreen(game, save));
                break;

            case "AHARIN":
                game.setScreen(new AharinScreen(game, save));
                break;

            default:
                game.setScreen(new GameScreen(game, save));
                break;
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();

        if (background != null) {
            background.dispose();
        }

        if (logo != null) {
            logo.dispose();
        }

        if (astronautTex != null) {
            astronautTex.dispose();
        }

        if (border != null) {
            border.dispose();
        }
    }
}
