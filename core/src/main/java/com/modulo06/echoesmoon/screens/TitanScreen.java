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
import com.modulo06.echoesmoon.systems.SoundManager;

public class TitanScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Rectangle player;
    private Enemy boss;
    private Array<Enemy> minions;
    private Array<SlashWave> slashesJogador;
    private Array<SlashWave> pedrasBoss;
    private Array<SlashWave> tirosMinions;
    private Array<ItemDrop> dropsO2; // << Array de drops de O2
    private DialogSystem dialog;

    private Texture fundoTex, playerSheet, slashTex, bossTex, rochaTex, portraitBoss, alienTex, o2Tex;
    private Animation<TextureRegion> playerAnim;
    private float stateTime = 0f;

    private int bossState = 0;
    private float bossTimer = 0f, hordeTimer = 0f, cooldown = 0f, reloadTimer = 0f, avisoTimer = 0f;
    private String mensagemAviso = "";
    private boolean isReloading = false;

    // Transição de Fade Overlay
    private float fadeAlpha = 1.0f;
    private boolean fadingOut = false;
    private Screen nextScreen = null;

    public TitanScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        dialog = new DialogSystem();

        player = new Rectangle(100, 100, 32, 48);

        // Boss Buffado: HP 4000
        boss = new Enemy(800, 800, 0);
        boss.rect.width = 110;
        boss.rect.height = 110;
        boss.maxHp = 4000;
        boss.hp = boss.maxHp;

        minions = new Array<>();
        slashesJogador = new Array<>();
        pedrasBoss = new Array<>();
        tirosMinions = new Array<>();
        dropsO2 = new Array<>();

        carregarTexturas();
        SoundManager.playMusic("boss_theme", true);

        dialog.start(new String[]{
            "HUMANO INSENSATO... COMO OUSA INVADIR MEU PLANETA?",
            "VOCÊ SERÁ PETRIFICADO COM AS MINHAS ROCHAS E LACRAIOS!",
            "TENTE VIR PRA CIMA DE MIM, VERME!"
        }, portraitBoss != null ? portraitBoss : bossTex);
    }

    private void carregarTexturas() {
        fundoTex = safeLoad("fundo_tita.png");
        playerSheet = safeLoad("player_marte.png");
        bossTex = safeLoad("boss_tita.png");
        alienTex = safeLoad("alien_lunar.png");
        slashTex = safeLoad("slash_wave.png");
        rochaTex = safeLoad("rocha_boss.png");
        portraitBoss = safeLoad("portrait_boss.png");
        o2Tex = safeLoad("o2.png");

        if (playerSheet != null) {
            TextureRegion[][] tmp = TextureRegion.split(playerSheet, playerSheet.getWidth() / 4, playerSheet.getHeight());
            TextureRegion[] walkFrames = new TextureRegion[4];
            for (int j = 0; j < 4; j++) walkFrames[j] = tmp[0][j];
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
        Gdx.gl.glClearColor(0.8f, 0.5f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, 1200, 1200);

        // Renderiza itens de O2 no chão
        for (ItemDrop d : dropsO2) {
            if (o2Tex != null) batch.draw(o2Tex, d.rect.x, d.rect.y, d.rect.width, d.rect.height);
        }

        // Renderiza Inimigos da Horda
        for (Enemy m : minions) {
            if (m.ativo && alienTex != null) batch.draw(alienTex, m.rect.x, m.rect.y, m.rect.width, m.rect.height);
        }

        if (boss.ativo && bossTex != null) batch.draw(bossTex, boss.rect.x, boss.rect.y, boss.rect.width, boss.rect.height);

        for (SlashWave s : slashesJogador) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        for (SlashWave p : pedrasBoss) if (p.active && rochaTex != null) batch.draw(rochaTex, p.rect.x, p.rect.y, 32, 32, 64, 64, 1.5f, 1.5f, p.angle, 0, 0, rochaTex.getWidth(), rochaTex.getHeight(), false, false);
        for (SlashWave t : tirosMinions) if (t.active && slashTex != null) batch.draw(slashTex, t.rect.x, t.rect.y, 16, 16, 32, 32, 0.8f, 0.8f, t.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);

        if (playerAnim != null) batch.draw(playerAnim.getKeyFrame(stateTime, true), player.x, player.y, player.width, player.height);
        else if (playerSheet != null) batch.draw(playerSheet, player.x, player.y, player.width, player.height);

        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Vida dos Minions
        for (Enemy m : minions) {
            if (m.ativo) {
                shapeRenderer.setColor(0.8f, 0f, 0f, 1);
                shapeRenderer.rect(m.rect.x, m.rect.y + m.rect.height + 3, m.rect.width, 4);
                shapeRenderer.setColor(0f, 0.8f, 0f, 1);
                shapeRenderer.rect(m.rect.x, m.rect.y + m.rect.height + 3, m.rect.width * Math.max(0, m.hp / (float)m.maxHp), 4);
            }
        }

        // Barra de Vida Gigante do Boss
        if (boss.ativo) {
            shapeRenderer.setColor(0.5f, 0f, 0f, 1f);
            shapeRenderer.rect(boss.rect.x, boss.rect.y + boss.rect.height + 8, boss.rect.width, 12);
            shapeRenderer.setColor(1f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(boss.rect.x, boss.rect.y + boss.rect.height + 8, boss.rect.width * Math.max(0, boss.hp / (float)boss.maxHp), 12);
        }
        shapeRenderer.end();

        desenharHUD();
        desenharFade(delta);
    }

    private void update(float delta) {
        if (fadingOut) return;

        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;
        saveData.o2 -= 0.8f * delta;
        if (saveData.o2 <= 0) game.setScreen(new GameOverScreen(game));

        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) dialog.next();
            return;
        }

        // Checkpoint por F5
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            saveData.fase = "TITA";
            saveData.salvar();
            mensagemAviso = "CHECKPOINT SALVO EM TITA!";
            avisoTimer = 2.0f;
        }

        // Coleta de O2 do chão
        for (int i = dropsO2.size - 1; i >= 0; i--) {
            ItemDrop d = dropsO2.get(i);
            if (player.overlaps(d.rect)) {
                saveData.o2 = Math.min(100, saveData.o2 + 20); // << VALOR DO O2 CONFIGURADO AQUI
                SoundManager.playSound("pickup");
                mensagemAviso = "+20 O2 COLETADO!";
                avisoTimer = 1.5f;
                dropsO2.removeIndex(i);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !isReloading && saveData.municao < 25) {
            isReloading = true; reloadTimer = 1.5f; mensagemAviso = "RECARREGANDO..."; avisoTimer = 1.5f;
            SoundManager.playSound("reload");
        }
        if (isReloading) {
            reloadTimer -= delta;
            if (reloadTimer <= 0) { isReloading = false; saveData.municao = 25; mensagemAviso = "MUNICÃO RECARREGADA!"; avisoTimer = 2f; }
        }

        boolean moving = false;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { player.x -= 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { player.x += 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { player.y += 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { player.y -= 300 * delta; moving = true; }
        if (moving) stateTime += delta;

        player.x = MathUtils.clamp(player.x, 0, 1200 - player.width);
        player.y = MathUtils.clamp(player.y, 0, 1200 - player.height);
        camera.position.set(player.x, player.y, 0);

        // Lógica do Boss
        if (boss.ativo) {
            bossTimer += delta;
            hordeTimer += delta;

            // Invocação de Horda (A cada 9 segundos)
            if (hordeTimer >= 9f) {
                hordeTimer = 0f;
                mensagemAviso = "O CHEFE CONVOCOU UMA HORDA!";
                avisoTimer = 2.5f;

                minions.add(new Enemy(boss.rect.x + 60, boss.rect.y, 0));
                minions.add(new Enemy(boss.rect.x - 60, boss.rect.y, 0));
                minions.add(new Enemy(boss.rect.x, boss.rect.y + 60, 1));
                minions.add(new Enemy(boss.rect.x, boss.rect.y - 60, 1));
            }

            if (bossState == 0) {
                Vector2 dir = new Vector2(player.x - boss.rect.x, player.y - boss.rect.y);
                if (dir.len() > 0) {
                    boss.rect.x += dir.nor().x * 240 * delta;
                    boss.rect.y += dir.nor().y * 240 * delta;
                }
                if (bossTimer > 2.5f) { bossState = 1; bossTimer = 0f; }
            } else if (bossState == 1) {
                if (bossTimer > 0.8f) {
                    pedrasBoss.add(new SlashWave(boss.rect.x + boss.rect.width/2, boss.rect.y + boss.rect.height/2, player.x, player.y));
                    bossState = 0; bossTimer = 0f;
                }
            }
            if (boss.rect.overlaps(player)) saveData.o2 -= 20f * delta;
        }

        // Atualização dos Minions
        for (int i = minions.size - 1; i >= 0; i--) {
            Enemy m = minions.get(i);
            if (m.ativo) {
                m.update(delta, new Vector2(player.x, player.y));
                if (m.type == 1 && m.cooldownTiro <= 0) {
                    tirosMinions.add(new SlashWave(m.rect.x, m.rect.y, player.x, player.y));
                    m.cooldownTiro = 3.0f;
                }
                if (m.rect.overlaps(player)) saveData.o2 -= 6f * delta;
            } else minions.removeIndex(i);
        }

        // Projéteis dos minions
        for (int i = tirosMinions.size - 1; i >= 0; i--) {
            SlashWave t = tirosMinions.get(i); t.update(delta);
            if (t.rect.overlaps(player)) { saveData.o2 -= 8f; t.active = false; }
            if (!t.active) tirosMinions.removeIndex(i);
        }

        for (int i = pedrasBoss.size - 1; i >= 0; i--) {
            SlashWave p = pedrasBoss.get(i); p.update(delta * 0.8f);
            if (p.rect.overlaps(player)) { saveData.o2 -= 25f; p.active = false; }
            if (!p.active) pedrasBoss.removeIndex(i);
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && saveData.municao > 0 && cooldown <= 0f && !isReloading) {
            saveData.municao--; cooldown = 0.25f;
            SoundManager.playSound("slash");
            Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(m); slashesJogador.add(new SlashWave(player.x, player.y, m.x, m.y));
        }

        for (int i = slashesJogador.size - 1; i >= 0; i--) {
            SlashWave s = slashesJogador.get(i); s.update(delta);
            if (!s.active) { slashesJogador.removeIndex(i); continue; }

            // Dano no Boss
            if (boss.ativo && s.rect.overlaps(boss.rect)) {
                boss.hp -= 25; s.active = false;
                if (boss.hp <= 0) {
                    boss.ativo = false;
                    fadingOut = true;
                    nextScreen = new VictoryScreen(game, saveData);
                }
                continue;
            }

            // Dano nos Minions e DROP DE O2
            for (Enemy m : minions) {
                if (m.ativo && s.rect.overlaps(m.rect)) {
                    m.hp -= 40; s.active = false;
                    if (m.hp <= 0) {
                        m.ativo = false;
                        // 60% de chance do minion dropar O2 ao morrer
                        if (MathUtils.randomBoolean(0.6f)) {
                            dropsO2.add(new ItemDrop(m.rect.x, m.rect.y, 0));
                        }
                    }
                    break;
                }
            }
        }
    }

    private void desenharHUD() {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.8f);
        shapeRenderer.rect(20, 20, 250, 100);
        shapeRenderer.setColor(0.1f, 0.5f, 0.8f, 1f);
        shapeRenderer.rect(30, 30, 230 * (Math.max(0, saveData.o2) / 100f), 15);
        shapeRenderer.end();

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "O2: " + (int)Math.max(0, saveData.o2), 30, 75);
        font.draw(batch, "MUNICÃO: " + saveData.municao, 30, 95);
        font.draw(batch, "PLANETA: TITA", 30, 115);

        font.draw(batch, "[TAB] Ocultar Objetivos | [F5] Salvar Checkpoint | [R] Recarregar", 20, 715);

        if (isReloading) font.draw(batch, "RECARREGANDO...", 130, 95);
        if (avisoTimer > 0) font.draw(batch, mensagemAviso, 550, 100);
        batch.end();

        dialog.render(batch, shapeRenderer, font);
    }

    private void desenharFade(float delta) {
        if (fadeAlpha > 0 || fadingOut) {
            if (fadingOut) fadeAlpha = Math.min(1.0f, fadeAlpha + delta * 2f);
            else fadeAlpha = Math.max(0.0f, fadeAlpha - delta * 2f);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, fadeAlpha);
            shapeRenderer.rect(0, 0, 1280, 720);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            if (fadingOut && fadeAlpha >= 1.0f && nextScreen != null) {
                game.setScreen(nextScreen);
            }
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {} @Override public void dispose() {}
}
