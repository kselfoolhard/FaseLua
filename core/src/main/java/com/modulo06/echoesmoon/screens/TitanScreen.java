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
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.WorldRock;
import com.modulo06.echoesmoon.entities.ItemDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SoundManager;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;

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
    private Array<ItemDrop> dropsO2;
    private Array<FoodDrop> comidas;
    private Array<WorldRock> pedrasMapa;
    private DialogSystem dialog;

    private Texture fundoTex, slashTex, bossTex, rochaTex, portraitBoss, alienTex, o2Tex, foodTex, pedraMapaTex;

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;
    private float somEnemyTimer = 0f;

    private int bossState = 0;
    private float bossTimer = 0f, hordeTimer = 0f, cooldown = 0f, reloadTimer = 0f, avisoTimer = 0f;
    private String mensagemAviso = "";
    private boolean isReloading = false;

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

        boss = new Enemy(800, 800, 0);
        boss.rect.width = 110;
        boss.rect.height = 110;
        boss.maxHp = 180;
        boss.hp = boss.maxHp;

        minions = new Array<>();
        slashesJogador = new Array<>();
        pedrasBoss = new Array<>();
        tirosMinions = new Array<>();
        dropsO2 = new Array<>();
        comidas = new Array<>();
        pedrasMapa = new Array<>();
        saveData.sincronizarInventario();

        carregarTexturas();
        spawnAmbientObjects();
        SoundManager.playMusic("boss", true);
        SoundManager.playSound("bossgrowl");

        dialog.start(new String[]{
            "HUMANO INSENSATO... VOCE OUSOU INVASIR O MEU DOMINIO!",
            "EU SOU A BESTA DE TITA! SEU OXIGENIO SERA O SEU FIM!"
        }, portraitBoss != null ? portraitBoss : bossTex);
    }

    private void carregarTexturas() {
        fundoTex = safeLoad("fundo_tita.png");
        bossTex = safeLoad("boss_tita.png");
        alienTex = safeLoad("alien_lunar.png");
        slashTex = safeLoad("slash_wave.png");
        rochaTex = safeLoad("rocha_boss.png");
        portraitBoss = safeLoad("portrait_boss.png");
        o2Tex = safeLoad("o2.png");
        foodTex = safeLoad("food.png");
        pedraMapaTex = safeLoad("pedra.png");

        Texture idleTex = safeLoad("player_lunar.png");
        Texture walkTex = safeLoad("player_lunar_walk.png");
        Texture slashTexAnim = safeLoad("player_lunar_slash.png");

        if (idleTex != null) animIdle = new Animation<>(0.2f, TextureRegion.split(idleTex, idleTex.getWidth() / 4, idleTex.getHeight())[0]);
        if (walkTex != null) animWalk = new Animation<>(0.12f, TextureRegion.split(walkTex, walkTex.getWidth() / 4, walkTex.getHeight())[0]);
        if (slashTexAnim != null) animSlash = new Animation<>(0.1f, TextureRegion.split(slashTexAnim, slashTexAnim.getWidth() / 4, slashTexAnim.getHeight())[0]);
    }


    private void spawnAmbientObjects() {
        Array<Rectangle> forbidden = new Array<>();
        forbidden.add(player);
        forbidden.add(boss.rect);
        WorldRock.spawnMany(pedrasMapa, 34, 1200f, 1200f, player, forbidden, 220f);
        for (int i = 0; i < 12; i++) {
            float x = MathUtils.random(70f, 1120f);
            float y = MathUtils.random(70f, 1120f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (r.overlaps(player) || r.overlaps(boss.rect)) continue;
            comidas.add(new FoodDrop(x, y));
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

        for (WorldRock rock : pedrasMapa) {
            if (pedraMapaTex != null) {
                batch.setColor(0.40f, 0.55f, 0.62f, 1f);
                batch.draw(pedraMapaTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        for (ItemDrop d : dropsO2) if (o2Tex != null) batch.draw(o2Tex, d.rect.x, d.rect.y, d.rect.width, d.rect.height);
        for (Enemy m : minions) if (m.ativo && alienTex != null) batch.draw(alienTex, m.rect.x, m.rect.y, m.rect.width, m.rect.height);
        if (boss.ativo) {
            if (bossTex != null) batch.draw(bossTex, boss.rect.x, boss.rect.y, boss.rect.width, boss.rect.height);
        }

        for (SlashWave s : slashesJogador) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        for (SlashWave p : pedrasBoss) if (p.active && rochaTex != null) batch.draw(rochaTex, p.rect.x, p.rect.y, 32, 32, 64, 64, 1.5f, 1.5f, p.angle, 0, 0, rochaTex.getWidth(), rochaTex.getHeight(), false, false);
        for (SlashWave t : tirosMinions) if (t.active && slashTex != null) batch.draw(slashTex, t.rect.x, t.rect.y, 16, 16, 32, 32, 0.8f, 0.8f, t.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);

        TextureRegion frameAtual = null;
        if (estadoJogador == 2 && animSlash != null) frameAtual = animSlash.getKeyFrame(stateTime, false);
        else if (estadoJogador == 1 && animWalk != null) frameAtual = animWalk.getKeyFrame(stateTime, true);
        else if (animIdle != null) frameAtual = animIdle.getKeyFrame(stateTime, true);

        if (frameAtual != null) batch.draw(frameAtual, player.x, player.y, player.width, player.height);
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (boss.ativo && bossTex == null) {
            shapeRenderer.setColor(bossState == 1 ? 1f : 0.8f, bossState == 1 ? 0.85f : 0.1f, 0.1f, 1f);
            shapeRenderer.rect(boss.rect.x, boss.rect.y, boss.rect.width, boss.rect.height);
        }

        for (Enemy m : minions) {
            if (m.ativo) {
                shapeRenderer.setColor(0.8f, 0f, 0f, 1);
                shapeRenderer.rect(m.rect.x, m.rect.y + m.rect.height + 5, m.rect.width, 5);
                shapeRenderer.setColor(0f, 0.8f, 0f, 1);
                shapeRenderer.rect(m.rect.x, m.rect.y + m.rect.height + 5, m.rect.width * Math.max(0, m.hp / (float)m.maxHp), 5);
            }
        }
        shapeRenderer.end();

        desenharHUD();
        desenharFade(delta);

        // --- SISTEMA DE INVENTARIO: RENDERIZA POR CIMA DE TUDO ---
        if (saveData.inventario != null && saveData.inventario.aberto) {
            saveData.inventario.render(batch, font, batch.getProjectionMatrix(), saveData);
        }
    }

    private void update(float delta) {
        if (fadingOut) return;

        // --- CONTROLE DO INVENTÁRIO & PAUSA ---
        if (saveData.inventario != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                saveData.inventario.aberto = !saveData.inventario.aberto;
            }
            if (saveData.inventario.aberto) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                    saveData.inventario.usarComida(saveData);
                }
                return; // PAUSA O JOGO AQUI
            }
        }

        if (somEnemyTimer > 0f) somEnemyTimer -= delta;
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;
        saveData.o2 -= 0.8f * delta;
        if (saveData.o2 <= 0) game.setScreen(new GameOverScreen(game));

        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) dialog.next();
            return;
        }

        if (estadoJogador == 2) {
            slashAnimTimer -= delta;
            if (slashAnimTimer <= 0) estadoJogador = 0;
        }

        boolean moving = false;
        float dx = 0f, dy = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { dx -= 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { dx += 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { dy += 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { dy -= 300 * delta; moving = true; }
        WorldCollision.movePlayer(player, dx, dy, pedrasMapa, 1200f, 1200f);
        stateTime += delta;

        if (estadoJogador != 2) {
            if (moving) {
                estadoJogador = 1;
                timerPasso -= delta;
                if (timerPasso <= 0) {
                    SoundManager.playSound(MathUtils.randomBoolean() ? "footstep1" : "footstep2");
                    timerPasso = 0.35f;
                }
            } else {
                estadoJogador = 0;
                timerPasso = 0f;
            }
        }

        player.x = MathUtils.clamp(player.x, 0, 1200 - player.width);
        player.y = MathUtils.clamp(player.y, 0, 1200 - player.height);
        camera.position.set(player.x, player.y, 0);

        for (int i = dropsO2.size - 1; i >= 0; i--) {
            ItemDrop d = dropsO2.get(i);
            if (player.overlaps(d.rect)) {
                saveData.o2 = Math.min(100, saveData.o2 + 20);
                SoundManager.playSound("pickup");
                mensagemAviso = "+20 O2 COLETADO!";
                avisoTimer = 1.5f;
                dropsO2.removeIndex(i);
            }
        }

        for (int i = comidas.size - 1; i >= 0; i--) {
            FoodDrop food = comidas.get(i);
            if (player.overlaps(food.rect)) {
                saveData.inventario.add("COMIDA");
                comidas.removeIndex(i);
                mensagemAviso = "+1 COMIDA! (+12 O2 ao usar)";
                avisoTimer = 2f;
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

        for (int i = minions.size - 1; i >= 0; i--) {
            Enemy m = minions.get(i);
            if (m.ativo) {
                m.update(delta, new Vector2(player.x, player.y));
                if (m.cooldownTiro <= 0 && new Vector2(player.x - m.rect.x, player.y - m.rect.y).len() < 500f) {
                    tirosMinions.add(new SlashWave(m.rect.x, m.rect.y, player.x, player.y));
                    m.cooldownTiro = 2.5f;
                }
                if (m.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, 10f * delta);
            } else {
                minions.removeIndex(i);
            }
        }

        if (boss.ativo) {
            bossTimer += delta;
            hordeTimer += delta;

            if (hordeTimer >= 9f) {
                hordeTimer = 0f;
                mensagemAviso = "O CHEFE CONVOCOU UMA HORDA!";
                avisoTimer = 2.5f;
                minions.add(new Enemy(boss.rect.x + 60, boss.rect.y, 1));
                minions.add(new Enemy(boss.rect.x - 60, boss.rect.y, 1));
                if (somEnemyTimer <= 0) {
                    SoundManager.playSound("enemy");
                    somEnemyTimer = 25f;
                }
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
            if (boss.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, 12f * delta);
        }

        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && saveData.municao > 0 && cooldown <= 0f && !isReloading) {
            estadoJogador = 2;
            slashAnimTimer = 0.3f;
            stateTime = 0f;
            saveData.municao--; saveData.inventario.municao = saveData.municao; cooldown = Math.max(0.14f, 0.25f - 0.02f * saveData.inventario.nivelArma);
            SoundManager.playSound("slash");

            Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(m);
            slashesJogador.add(new SlashWave(player.x, player.y, m.x, m.y));
        }

        for (int i = slashesJogador.size - 1; i >= 0; i--) {
            SlashWave s = slashesJogador.get(i); s.update(delta);
            if (!s.active) { slashesJogador.removeIndex(i); continue; }

            if (boss.ativo && s.rect.overlaps(boss.rect)) {
                boss.hp -= UpgradeSystem.danoArma(saveData); s.active = false;
                SoundManager.playSound("hit_enemy");
                if (boss.hp <= 0) {
                    boss.ativo = false;
                    saveData.bossTitaDerrotado = true;
                    saveData.inventario.add("CHAVE_TITA");
                    saveData.fase = "CALISTO";
                    saveData.salvar();
                    fadingOut = true; nextScreen = new CallistoScreen(game, saveData);
                }
                continue;
            }

            for (Enemy m : minions) {
                if (m.ativo && s.rect.overlaps(m.rect)) {
                    m.hp -= UpgradeSystem.danoArma(saveData); s.active = false;
                    SoundManager.playSound("hit_enemy");

                    if (m.hp <= 0) {
                        m.ativo = false;
                        if (UpgradeSystem.registerKill(saveData)) { mensagemAviso = saveData.ultimoUpgrade; avisoTimer = 2.5f; }
                        if (MathUtils.randomBoolean(0.5f)) {
                            dropsO2.add(new ItemDrop(m.rect.x, m.rect.y, 0));
                        }
                        // --- Adicionada chance do lacaio do boss dropar COMIDA ---
                        if (saveData.inventario != null && MathUtils.randomBoolean(0.3f)) saveData.inventario.add("COMIDA");
                    }
                    break;
                }
            }
        }

        for (int i = pedrasBoss.size - 1; i >= 0; i--) {
            SlashWave p = pedrasBoss.get(i);
            p.update(delta);
            if (p.rect.overlaps(player)) {
                UpgradeSystem.aplicarDano(saveData, 15f);
                p.active = false;
            }
            if (!p.active) pedrasBoss.removeIndex(i);
        }

        for (int i = tirosMinions.size - 1; i >= 0; i--) {
            SlashWave t = tirosMinions.get(i);
            t.update(delta);
            if (t.rect.overlaps(player)) {
                UpgradeSystem.aplicarDano(saveData, 10f);
                t.active = false;
            }
            if (!t.active) tirosMinions.removeIndex(i);
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

        if (boss.ativo) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f); shapeRenderer.rect(440, 680, 400, 20);
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(440, 680, 400 * (boss.hp / (float)boss.maxHp), 20);
        }
        shapeRenderer.end();

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "O2: " + (int)Math.max(0, saveData.o2), 30, 75);
        font.draw(batch, "MUNICÃO: " + saveData.municao, 30, 95);
        font.draw(batch, "PLANETA: TITA", 30, 115);

        if (boss.ativo) font.draw(batch, "BESTA DE TITA", 600, 695);

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
    @Override public void pause() {} @Override public void resume() {}

    @Override
    public void hide() { SoundManager.stopMusic(); }

    @Override
    public void dispose() { batch.dispose(); shapeRenderer.dispose(); font.dispose(); }
}
