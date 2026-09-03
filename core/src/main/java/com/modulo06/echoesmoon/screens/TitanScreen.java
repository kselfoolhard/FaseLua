package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.InimigoTita;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;

public class TitanScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture bossTex, fundoTitaTex, slashTex, playerSheet;
    private Animation<TextureRegion> playerAnim;
    private float stateTime = 0f;
    private Rectangle player;
    private Array<SlashWave> slashes;

    private InimigoTita boss;
    private DialogSystem dialog = new DialogSystem();

    private final float WORLD_WIDTH = 1000f;
    private final float WORLD_HEIGHT = 1000f;

    private float reloadTimer = 0f, cooldown = 0f, avisoTimer = 0f;
    private boolean mostrarQuest = true;
    private String mensagemAviso = "";

    public TitanScreen(Game game, GameSaveData saveData) {
        this.game = game; this.saveData = saveData;
        camera = new OrthographicCamera(); camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch(); shapeRenderer = new ShapeRenderer(); font = new BitmapFont();

        player = new Rectangle(300, 300, 32, 48);
        slashes = new Array<>();

        boss = new InimigoTita();
        boss.pos.set(WORLD_WIDTH / 2, WORLD_HEIGHT - 200);
        boss.hp = 300;
        boss.speed = 100f; // Boss Velocity control

        carregarTexturas();
        dialog.start(new String[]{"O METANO CONSOME TUDO.", "PREPARE-SE PARA O SEU FIM!"});
    }

    private void carregarTexturas() {
        bossTex = safeLoad("boss_tita.png"); fundoTitaTex = safeLoad("fundo_tita.png");
        slashTex = safeLoad("slash_wave.png"); playerSheet = safeLoad("player_marte.png");
        if (playerSheet == null) playerSheet = safeLoad("player_lua.png");
        if (playerSheet != null) {
            TextureRegion[][] tmp = TextureRegion.split(playerSheet, playerSheet.getWidth() / 4, playerSheet.getHeight());
            TextureRegion[] walkFrames = new TextureRegion[4]; int index = 0;
            for (int j = 0; j < 4; j++) walkFrames[index++] = tmp[0][j];
            playerAnim = new Animation<>(0.15f, walkFrames);
        }
    }

    private Texture safeLoad(String path) {
        try { if (Gdx.files.internal(path).exists()) return new Texture(path); } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void render(float delta) {
        update(delta);
        if (fundoTitaTex != null) Gdx.gl.glClearColor(0, 0, 0, 1);
        else Gdx.gl.glClearColor(0.75f, 0.35f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // 1. TEXTURAS
        batch.setProjectionMatrix(camera.combined); batch.begin();
        if (fundoTitaTex != null) batch.draw(fundoTitaTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (boss.vivo() && bossTex != null) batch.draw(bossTex, boss.bounds.x, boss.bounds.y, boss.bounds.width * 2, boss.bounds.height * 2);

        for (SlashWave s : slashes) {
            if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 32, 32, 64, 64, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        }
        if (playerAnim != null) batch.draw(playerAnim.getKeyFrame(stateTime, true), player.x, player.y, player.width, player.height);
        else if (playerSheet != null) batch.draw(playerSheet, player.x, player.y, player.width, player.height);
        batch.end();

        // 2. FORMAS GEOMÉTRICAS (Barra HP Boss)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (boss.vivo()) {
            shapeRenderer.setColor(0.8f, 0f, 0f, 1);
            shapeRenderer.rect(boss.pos.x, boss.pos.y + boss.bounds.height + 10, boss.bounds.width, 8);
            shapeRenderer.setColor(0f, 0.8f, 0f, 1);
            float bossHpPercent = Math.max(0, (float)boss.hp / 300f);
            shapeRenderer.rect(boss.pos.x, boss.pos.y + boss.bounds.height + 10, boss.bounds.width * bossHpPercent, 8);
        }
        shapeRenderer.end();

        desenharHUDL4D();
    }

    private void desenharHUDL4D() {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (mostrarQuest) {
            shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.6f); shapeRenderer.rect(20, 580, 350, 120);
            shapeRenderer.setColor(1f, 1f, 1f, 0.8f); shapeRenderer.rectLine(20, 580, 20, 700, 4);
        }

        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.6f); shapeRenderer.rect(20, 20, 250, 80);
        shapeRenderer.setColor(0.1f, 0.8f, 0.2f, 1f); shapeRenderer.rect(30, 30, 230 * (Math.max(0, saveData.o2) / 100f), 15);

        if (dialog.isOpen()) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.9f); shapeRenderer.rect(200, 500, 880, 150);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.rectLine(200, 500, 1080, 500, 4); shapeRenderer.rectLine(200, 650, 1080, 650, 4);
            shapeRenderer.rectLine(200, 500, 200, 650, 4); shapeRenderer.rectLine(1080, 500, 1080, 650, 4);
        }
        shapeRenderer.end(); Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin(); font.setColor(1, 1, 1, 1);
        font.draw(batch, "+ " + (int)Math.max(0, saveData.o2), 30, 75);

        font.getData().setScale(1.5f);
        font.draw(batch, (reloadTimer > 0) ? "RECARREGANDO..." : String.valueOf(saveData.municao), 1150, 350);
        font.getData().setScale(1f);

        if (mostrarQuest) {
            font.draw(batch, "OBJETIVOS ATUAIS", 40, 680);
            font.draw(batch, "- Elimine a ameaca de Tita.", 40, 650);
            font.draw(batch, "- Sobreviva ao metano.", 40, 630);
        }
        font.draw(batch, "[TAB] Ocultar Objetivos | [F5] Salvar", 20, 715);

        if (avisoTimer > 0) font.draw(batch, mensagemAviso, 600, 100);
        if (dialog.isOpen()) font.draw(batch, "* " + dialog.line(), 230, 600);
        batch.end();
    }

    private void update(float delta) {
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;
        if (reloadTimer > 0f) reloadTimer -= delta;

        boolean isTalking = dialog.isOpen();

        saveData.o2 -= 1.8f * delta;
        if (saveData.o2 <= 0) { game.setScreen(new GameOverScreen(game)); return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) mostrarQuest = !mostrarQuest;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) { saveData.salvar(); mensagemAviso = "JOGO SALVO!"; avisoTimer = 2f; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && reloadTimer <= 0f && saveData.municao < 25) { saveData.municao = 25; reloadTimer = 2.0f; }

        if (isTalking) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) dialog.next();
        } else {
            boolean moving = false;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { player.x -= 300 * delta; moving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { player.x += 300 * delta; moving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { player.y += 300 * delta; moving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { player.y -= 300 * delta; moving = true; }
            if (moving) stateTime += delta;

            player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
            player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);
            camera.position.set(player.x, player.y, 0);

            // A MÁGICA DA IA AQUI: O método update da classe InimigoTita faz a perseguição
            boss.update(delta, new Vector2(player.x, player.y));
            if (boss.vivo() && player.overlaps(boss.bounds)) saveData.o2 -= 20f * delta;

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && saveData.municao > 0 && cooldown <= 0f && reloadTimer <= 0f) {
                saveData.municao--; cooldown = 0.25f;
                Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(m); slashes.add(new SlashWave(player.x, player.y, m.x, m.y));
            }

            for (int i = slashes.size - 1; i >= 0; i--) {
                SlashWave s = slashes.get(i); s.update(delta);
                if (!s.active) { slashes.removeIndex(i); continue; }

                if (boss.vivo() && s.rect.overlaps(boss.bounds)) {
                    boss.hp -= 35; s.active = false;
                    if (!boss.vivo()) { GameSaveData.apagar(); game.setScreen(new EndGameScreen(game, "TITA DOMINADO! VOCE VENCEU!")); }
                }
            }
        }
    }
    @Override public void show(){} @Override public void resize(int w, int h){} @Override public void pause(){} @Override public void resume(){} @Override public void hide(){} @Override public void dispose(){}
}
