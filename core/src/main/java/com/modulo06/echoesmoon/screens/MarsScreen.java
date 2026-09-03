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
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.ItemDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;

public class MarsScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture fundoTex, alienTex, portalTex, o2Tex, tiroAlienTex, slashTex, playerSheet, npcTex;
    private Animation<TextureRegion> playerAnim;
    private float stateTime = 0f;

    private Rectangle player, portalParaTita, npcRect;
    private Array<Enemy> enemies;
    private Array<SlashWave> slashes;
    private Array<ItemDrop> drops;

    private class TiroAlien {
        Rectangle rect; Vector2 vel; boolean ativo = true;
        public TiroAlien(float x, float y, Vector2 dir) { rect = new Rectangle(x,y,12,12); vel = dir.nor().scl(200f); }
        public void update(float delta) { rect.x += vel.x * delta; rect.y += vel.y * delta; }
    }
    private Array<TiroAlien> projeteisAlien;
    private DialogSystem dialog = new DialogSystem();

    private final float WORLD_WIDTH = 1200f;
    private final float WORLD_HEIGHT = 1200f;

    private float cooldown = 0f, reloadTimer = 0f, avisoTimer = 0f;
    private int waveState = 1;
    private float waveTimer = 25f, spawnTimer = 0f;
    private String mensagemAviso = "";

    private boolean wavesIniciadas = false;
    private boolean titaLiberado = false, mostrarQuest = true;

    public MarsScreen(Game game, GameSaveData saveData) {
        this.game = game; this.saveData = saveData;
        camera = new OrthographicCamera(); camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch(); shapeRenderer = new ShapeRenderer(); font = new BitmapFont();

        player = new Rectangle(300, 300, 32, 48);
        npcRect = new Rectangle(600, 600, 32, 48); // Posição do NPC
        portalParaTita = new Rectangle(1000, 1000, 100, 100);

        slashes = new Array<>(); drops = new Array<>(); enemies = new Array<>();
        projeteisAlien = new Array<>();
        carregarTexturas();
    }

    private void carregarTexturas() {
        fundoTex = safeLoad("fundo_marte.png"); alienTex = safeLoad("alien.png");
        portalTex = safeLoad("portal.png"); o2Tex = safeLoad("o2.png");
        slashTex = safeLoad("slash_wave.png"); tiroAlienTex = safeLoad("tiro_alien.png");
        npcTex = safeLoad("npc.png");

        playerSheet = safeLoad("player_marte.png");
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
        Gdx.gl.glClearColor(0.6f, 0.2f, 0.1f, 1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // 1. TEXTURAS
        batch.setProjectionMatrix(camera.combined); batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (!wavesIniciadas) {
            if (npcTex != null) batch.draw(npcTex, npcRect.x, npcRect.y, npcRect.width, npcRect.height);
            else font.draw(batch, "[NPC]", npcRect.x, npcRect.y + 60);
        }

        if (titaLiberado && portalTex != null) batch.draw(portalTex, portalParaTita.x, portalParaTita.y, portalParaTita.width, portalParaTita.height);

        for (ItemDrop drop : drops) {
            if (o2Tex != null) batch.draw(o2Tex, drop.rect.x, drop.rect.y, drop.rect.width, drop.rect.height);
        }
        for (Enemy e : enemies) {
            if (e.ativo && alienTex != null) batch.draw(alienTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        }
        for (TiroAlien t : projeteisAlien) {
            if (t.ativo) {
                Texture tex = tiroAlienTex != null ? tiroAlienTex : o2Tex;
                if (tex != null) batch.draw(tex, t.rect.x, t.rect.y, t.rect.width, t.rect.height);
            }
        }
        for (SlashWave s : slashes) {
            if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 32, 32, 64, 64, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        }
        if (playerAnim != null) batch.draw(playerAnim.getKeyFrame(stateTime, true), player.x, player.y, player.width, player.height);
        else if (playerSheet != null) batch.draw(playerSheet, player.x, player.y, player.width, player.height);
        batch.end();

        // 2. BARRAS DE VIDA (ShapeRenderer)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            if (e.ativo) {
                shapeRenderer.setColor(0.8f, 0f, 0f, 1);
                shapeRenderer.rect(e.rect.x, e.rect.y + e.rect.height + 5, e.rect.width, 5);
                shapeRenderer.setColor(0f, 0.8f, 0f, 1);
                float hpPercent = Math.max(0, e.hp / 100f);
                shapeRenderer.rect(e.rect.x, e.rect.y + e.rect.height + 5, e.rect.width * hpPercent, 5);
            }
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

        // Fundo do Dialog
        if (dialog.isOpen()) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.9f); shapeRenderer.rect(200, 500, 880, 150);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.rectLine(200, 500, 1080, 500, 4); shapeRenderer.rectLine(200, 650, 1080, 650, 4);
            shapeRenderer.rectLine(200, 500, 200, 650, 4); shapeRenderer.rectLine(1080, 500, 1080, 650, 4);
        }
        shapeRenderer.end(); Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "+ " + (int)Math.max(0, saveData.o2), 30, 75);
        font.getData().setScale(1.5f);
        font.draw(batch, (reloadTimer > 0) ? "RECARREGANDO..." : String.valueOf(saveData.municao), 1150, 350);
        font.getData().setScale(1f);

        if (mostrarQuest) {
            font.draw(batch, "OBJETIVOS ATUAIS", 40, 680);
            if (!wavesIniciadas) font.draw(batch, "- Fale com o Comandante (NPC).", 40, 650);
            else font.draw(batch, "- Sobreviva as ondas (" + (Math.min(waveState, 3)) + "/3).", 40, 650);
            if (titaLiberado) font.draw(batch, "- Fuja pelo portal para Tita!", 40, 610);
        }

        font.draw(batch, "[TAB] Ocultar Objetivos | [F5] Salvar", 20, 715);
        if (avisoTimer > 0) font.draw(batch, mensagemAviso, 600, 100);

        if (dialog.isOpen()) font.draw(batch, "* " + dialog.line(), 230, 600);

        batch.setProjectionMatrix(camera.combined);
        if (!wavesIniciadas && !dialog.isOpen() && player.overlaps(npcRect)) font.draw(batch, "[E] FALAR", npcRect.x, npcRect.y - 10);
        if (titaLiberado && player.overlaps(portalParaTita)) font.draw(batch, "[E] IR PARA TITA", portalParaTita.x, portalParaTita.y - 20);
        batch.end();
    }

    private void update(float delta) {
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;
        if (reloadTimer > 0f) reloadTimer -= delta;

        boolean isTalking = dialog.isOpen();
        saveData.o2 -= 1.0f * delta;
        if (saveData.o2 <= 0) { game.setScreen(new GameOverScreen(game)); return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) mostrarQuest = !mostrarQuest;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) { saveData.salvar(); mensagemAviso = "JOGO SALVO!"; avisoTimer = 2.0f; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && reloadTimer <= 0f && saveData.municao < 25) { saveData.municao = 25; reloadTimer = 2.0f; }

        if (isTalking) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                dialog.next();
                if (!dialog.isOpen()) wavesIniciadas = true; // Inicia as waves ao fechar o dialogo
            }
        } else {
            // Movimentação do Jogador
            boolean moving = false;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { player.x -= 300 * delta; moving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { player.x += 300 * delta; moving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { player.y += 300 * delta; moving = true; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { player.y -= 300 * delta; moving = true; }
            if (moving) stateTime += delta;

            player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
            player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);
            camera.position.set(player.x, player.y, 0);

            // Interação com NPC
            if (!wavesIniciadas && player.overlaps(npcRect) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialog.start(new String[]{
                    "ATENCAO RECRUTA! Os alienigenas detectaram nossa presenca.",
                    "Eles atacarao em 3 grandes ondas de choque.",
                    "Nao deixe que destruam seu traje. Sobreviva e fuja!"
                });
            }

            // Lógica das Waves
            if (wavesIniciadas && waveState <= 3) {
                waveTimer -= delta; spawnTimer += delta;
                if (spawnTimer >= 1.5f - (waveState * 0.2f)) {
                    enemies.add(new Enemy(MathUtils.random(100, WORLD_WIDTH - 100), MathUtils.random(100, WORLD_HEIGHT - 100), 0));
                    spawnTimer = 0f;
                }
                if (waveTimer <= 0) {
                    waveState++; waveTimer = 25f;
                    if (waveState > 3) { titaLiberado = true; mensagemAviso = "PORTAL TITA ABERTO!"; avisoTimer = 4f; }
                }
            }

            // IA e Colisões
            for (Enemy e : enemies) {
                if (e.ativo) {
                    Vector2 dir = new Vector2(player.x - e.rect.x, player.y - e.rect.y);
                    if (dir.len() > 0) { e.rect.x += dir.nor().x * 55 * delta; e.rect.y += dir.nor().y * 55 * delta; }
                    if (MathUtils.random() < 0.01f) projeteisAlien.add(new TiroAlien(e.rect.x, e.rect.y, new Vector2(player.x - e.rect.x, player.y - e.rect.y)));
                    if (e.rect.overlaps(player)) saveData.o2 -= 10f * delta;
                }
            }

            for (int i = projeteisAlien.size - 1; i >= 0; i--) {
                TiroAlien t = projeteisAlien.get(i); t.update(delta);
                if (t.rect.overlaps(player)) { saveData.o2 -= 10f; t.ativo = false; projeteisAlien.removeIndex(i); }
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && saveData.municao > 0 && cooldown <= 0f && reloadTimer <= 0f) {
                saveData.municao--; cooldown = 0.25f;
                Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(m); slashes.add(new SlashWave(player.x, player.y, m.x, m.y));
            }

            for (int i = slashes.size - 1; i >= 0; i--) {
                SlashWave s = slashes.get(i); s.update(delta);
                if (!s.active) { slashes.removeIndex(i); continue; }
                for (Enemy e : enemies) {
                    if (e.ativo && s.rect.overlaps(e.rect)) {
                        e.hp -= 40; s.active = false;
                        if (e.hp <= 0) {
                            e.ativo = false;
                            if (MathUtils.randomBoolean(0.3f)) drops.add(new ItemDrop(e.rect.x, e.rect.y, 0));
                        }
                        break;
                    }
                }
            }

            for (int i = drops.size - 1; i >= 0; i--) {
                ItemDrop drop = drops.get(i);
                if (player.overlaps(drop.rect)) { saveData.o2 = Math.min(100, saveData.o2 + 15); drops.removeIndex(i); }
            }

            if (titaLiberado && Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(portalParaTita)) {
                saveData.fase = "TITA"; saveData.salvar(); game.setScreen(new TitanScreen(game, saveData));
            }
        }
    }
    @Override public void show(){} @Override public void resize(int w, int h){} @Override public void pause(){} @Override public void resume(){} @Override public void hide(){} @Override public void dispose(){}
}
