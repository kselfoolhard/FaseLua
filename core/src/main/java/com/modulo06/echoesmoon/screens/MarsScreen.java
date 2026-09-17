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

public class MarsScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Rectangle player, npcRadio, portalTita;
    private Array<Enemy> enemies;
    private Array<SlashWave> slashes;
    private Array<SlashWave> tirosInimigos;
    private Array<ItemDrop> dropsO2;
    private Array<FoodDrop> comidas;
    private Array<WorldRock> pedras;
    private DialogSystem dialog;

    private Texture fundoTex, slashTex, alienTex, radioTex, portraitRadio, portalTitaTex, o2Tex, foodTex, rockTex;

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;
    private float somEnemyTimer = 0f;

    private boolean wavesIniciadas = false;
    private boolean titaLiberado = false;
    private int waveState = 1;
    private int inimigosMortosNaWave = 0;
    private int totalInimigosNaWave = 5;

    private float spawnTimer = 0f, cooldown = 0f;
    private float reloadTimer = 0f, avisoTimer = 0f;
    private String mensagemAviso = "";
    private boolean isReloading = false;

    private float fadeAlpha = 1.0f;
    private boolean fadingOut = false;
    private Screen nextScreen = null;

    public MarsScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        dialog = new DialogSystem();

        player = new Rectangle(400, 300, 32, 48);
        npcRadio = new Rectangle(200, 200, 64, 64);
        portalTita = new Rectangle(1000, 500, 100, 100);

        enemies = new Array<>();
        slashes = new Array<>();
        tirosInimigos = new Array<>();
        dropsO2 = new Array<>();
        comidas = new Array<>();
        pedras = new Array<>();
        saveData.sincronizarInventario();

        carregarTexturas();
        spawnAmbientObjects();
        SoundManager.playMusic("marte", true);
    }

    private void carregarTexturas() {
        fundoTex = safeLoad("fundo_marte.png");
        slashTex = safeLoad("slash_wave.png");
        alienTex = safeLoad("alien.png");
        radioTex = safeLoad("radio.png");
        portraitRadio = safeLoad("portrait_radio.png");
        portalTitaTex = safeLoad("portal_tita.png");
        o2Tex = safeLoad("o2.png");
        foodTex = safeLoad("food.png");
        rockTex = safeLoad("pedra.png");

        Texture idleTex = safeLoad("player_lunar.png");
        Texture walkTex = safeLoad("player_lunar_walk.png");
        Texture slashTexAnim = safeLoad("player_lunar_slash.png");

        if (idleTex != null) animIdle = new Animation<>(0.2f, TextureRegion.split(idleTex, idleTex.getWidth() / 4, idleTex.getHeight())[0]);
        if (walkTex != null) animWalk = new Animation<>(0.12f, TextureRegion.split(walkTex, walkTex.getWidth() / 4, walkTex.getHeight())[0]);
        if (slashTexAnim != null) animSlash = new Animation<>(0.1f, TextureRegion.split(slashTexAnim, slashTexAnim.getWidth() / 4, slashTexAnim.getHeight())[0]);
    }


    private void spawnAmbientObjects() {
        Array<Rectangle> forbidden = new Array<>();
        forbidden.add(npcRadio);
        forbidden.add(portalTita);
        WorldRock.spawnMany(pedras, 30, 1200f, 1200f, player, forbidden, 180f);
        for (int i = 0; i < 11; i++) {
            float x = MathUtils.random(80f, 1120f);
            float y = MathUtils.random(80f, 1120f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (r.overlaps(player) || r.overlaps(npcRadio) || r.overlaps(portalTita)) continue;
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
        Gdx.gl.glClearColor(0.6f, 0.2f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, 1200, 1200);
        if (radioTex != null) batch.draw(radioTex, npcRadio.x, npcRadio.y, npcRadio.width, npcRadio.height);

        if (titaLiberado) {
            if (portalTitaTex != null) batch.draw(portalTitaTex, portalTita.x, portalTita.y, portalTita.width, portalTita.height);
            font.draw(batch, "PORTAL TITA", portalTita.x + 10, portalTita.y + 120);
        }

        for (WorldRock rock : pedras) {
            if (rockTex != null) {
                batch.setColor(0.68f, 0.46f, 0.38f, 1f);
                batch.draw(rockTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        for (ItemDrop d : dropsO2) if (o2Tex != null) batch.draw(o2Tex, d.rect.x, d.rect.y, d.rect.width, d.rect.height);
        for (Enemy e : enemies) if (e.ativo && alienTex != null) batch.draw(alienTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);

        for (SlashWave s : tirosInimigos) {
            if (s.active && slashTex != null) {
                batch.setColor(1, 0.2f, 0.2f, 1);
                batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 0.7f, 0.7f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
                batch.setColor(1, 1, 1, 1);
            }
        }
        for (SlashWave s : slashes) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);

        TextureRegion frameAtual = null;
        if (estadoJogador == 2 && animSlash != null) frameAtual = animSlash.getKeyFrame(stateTime, false);
        else if (estadoJogador == 1 && animWalk != null) frameAtual = animWalk.getKeyFrame(stateTime, true);
        else if (animIdle != null) frameAtual = animIdle.getKeyFrame(stateTime, true);

        if (frameAtual != null) batch.draw(frameAtual, player.x, player.y, player.width, player.height);

        if (player.overlaps(npcRadio) && !wavesIniciadas && !dialog.isOpen()) font.draw(batch, "[E] LIGAR RADIO", npcRadio.x - 20, npcRadio.y + 80);
        if (player.overlaps(portalTita) && titaLiberado) font.draw(batch, "[E] IR PARA TITA", portalTita.x, portalTita.y - 20);
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            if (e.ativo) {
                shapeRenderer.setColor(0.8f, 0f, 0f, 1);
                shapeRenderer.rect(e.rect.x, e.rect.y + e.rect.height + 5, e.rect.width, 5);
                shapeRenderer.setColor(0f, 0.8f, 0f, 1);
                shapeRenderer.rect(e.rect.x, e.rect.y + e.rect.height + 5, e.rect.width * Math.max(0, e.hp / (float)e.maxHp), 5);
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
        saveData.o2 -= 0.5f * delta;
        if (saveData.o2 <= 0) game.setScreen(new GameOverScreen(game));

        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                dialog.next();
                if (!dialog.isOpen() && player.overlaps(npcRadio) && !wavesIniciadas) {
                    wavesIniciadas = true;
                    SoundManager.playMusic("horda", true);
                }
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            saveData.fase = "MARTE"; saveData.salvar();
            mensagemAviso = "CHECKPOINT SALVO EM MARTE!"; avisoTimer = 2.0f;
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
        WorldCollision.movePlayer(player, dx, dy, pedras, 1200f, 1200f);
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
                mensagemAviso = "+20 O2 COLETADO!"; avisoTimer = 1.5f;
                dropsO2.removeIndex(i);
            }
        }

        if (!wavesIniciadas && player.overlaps(npcRadio) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            dialog.start(new String[]{
                "Aqui é o Oficial! O portal para Titã esta instavel.",
                "Os Aliens Marcianos atiram projeteis! Cuidado!",
                "Ao elimina-los, eles derrubam tanques de O2 no chão!",
                "Quando eliminar todos, Vá para o Portal de Titã"
            }, portraitRadio);
        }

        if (wavesIniciadas && waveState <= 3) {
            spawnTimer += delta;
            if (spawnTimer >= 1.5f && enemies.size < totalInimigosNaWave - inimigosMortosNaWave) {
                enemies.add(new Enemy(MathUtils.random(100, 1100), MathUtils.random(100, 1100), 1));
                if (somEnemyTimer <= 0) {
                    SoundManager.playSound("enemy");
                    somEnemyTimer = 25f;
                }
                spawnTimer = 0f;
            }
            if (inimigosMortosNaWave >= totalInimigosNaWave) {
                waveState++; inimigosMortosNaWave = 0; totalInimigosNaWave += 3;
                if (waveState > 3) {
                    titaLiberado = true;
                    saveData.marteMissoesOk = true;
                    saveData.fase = "MARTE_BOSS";
                    saveData.salvar();
                    SoundManager.playMusic("marte", true);
                }
            }
        }

        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && saveData.municao > 0 && cooldown <= 0f && !isReloading) {
            estadoJogador = 2;
            slashAnimTimer = 0.3f;
            stateTime = 0f;
            saveData.municao--; saveData.inventario.municao = saveData.municao; cooldown = Math.max(0.14f, 0.25f - 0.02f * saveData.inventario.nivelArma);
            SoundManager.playSound("slash");
            Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(m); slashes.add(new SlashWave(player.x, player.y, m.x, m.y));
        }

        for (int i = slashes.size - 1; i >= 0; i--) {
            SlashWave s = slashes.get(i); s.update(delta);
            if (!s.active) { slashes.removeIndex(i); continue; }
            for (Enemy e : enemies) {
                if (e.ativo && s.rect.overlaps(e.rect)) {
                    e.hp -= UpgradeSystem.danoArma(saveData); s.active = false;
                    if (e.hp <= 0) {
                        e.ativo = false; inimigosMortosNaWave++;
                        if (UpgradeSystem.registerKill(saveData)) { mensagemAviso = saveData.ultimoUpgrade; avisoTimer = 2.5f; }
                        if (MathUtils.randomBoolean(0.5f)) dropsO2.add(new ItemDrop(e.rect.x, e.rect.y, 0));
                        // --- Adicionada chance do inimigo dropar COMIDA para o Inventário ---
                        if (saveData.inventario != null && MathUtils.randomBoolean(0.3f)) saveData.inventario.add("COMIDA");
                    }
                }
            }
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (e.ativo) {
                e.update(delta, new Vector2(player.x, player.y));
                if (e.cooldownTiro <= 0 && new Vector2(player.x - e.rect.x, player.y - e.rect.y).len() < 500f) {
                    tirosInimigos.add(new SlashWave(e.rect.x, e.rect.y, player.x, player.y));
                    e.cooldownTiro = 2.5f;
                }
                if (e.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, 10f * delta);
            } else enemies.removeIndex(i);
        }

        for (int i = tirosInimigos.size - 1; i >= 0; i--) {
            SlashWave s = tirosInimigos.get(i); s.update(delta);
            if (s.rect.overlaps(player)) { UpgradeSystem.aplicarDano(saveData, 10f); s.active = false; }
            if (!s.active) tirosInimigos.removeIndex(i);
        }

        if (titaLiberado && player.overlaps(portalTita) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            saveData.fase = "MARTE_BOSS";
            saveData.salvar();
            fadingOut = true; nextScreen = new BossMarteScreen(game, saveData);
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

        if (wavesIniciadas && waveState <= 3) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f); shapeRenderer.rect(440, 680, 400, 20);
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(440, 680, 400 * ((float) inimigosMortosNaWave / totalInimigosNaWave), 20);
        }
        shapeRenderer.end();

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "O2: " + (int)Math.max(0, saveData.o2), 30, 75);
        font.draw(batch, "MUNICÃO: " + saveData.municao, 30, 95);
        font.draw(batch, "PLANETA: MARTE", 30, 115);

        font.draw(batch, "[TAB] Ocultar Quest | [I] Inventario | [F5] Salvar Checkpoint | [R] Recarregar", 20, 715);

        if (isReloading) font.draw(batch, "RECARREGANDO...", 130, 95);
        if (avisoTimer > 0) font.draw(batch, mensagemAviso, 550, 100);

        if (wavesIniciadas && waveState <= 3) font.draw(batch, "ONDA " + waveState + " - INIMIGOS RESTANTES: " + (totalInimigosNaWave - inimigosMortosNaWave), 450, 670);
        else if (titaLiberado) font.draw(batch, "SISTEMA SEGURO. PORTAL LIBERADO.", 500, 690);
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
