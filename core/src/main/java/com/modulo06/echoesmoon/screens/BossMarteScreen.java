package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Cursor;
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
import com.modulo06.echoesmoon.entities.BossMarte;
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.ItemDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.BossBalance;
import com.modulo06.echoesmoon.systems.CrosshairUtil;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SoundManager;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;
import com.modulo06.echoesmoon.systems.PlayerCombat;
import com.modulo06.echoesmoon.systems.GameHud;
import com.modulo06.echoesmoon.systems.LoadingOverlay;
import com.modulo06.echoesmoon.systems.RouteSystem;
import com.modulo06.echoesmoon.systems.RecoverySystems;

/**
 * Titan-Ferrugem, chefe de Marte — mesmo sistema da TitanScreen (e da
 * BossLuaScreen), so trocando sprites/mensagens. Fica no meio da progressao:
 * mais forte que o Guardiao da Lua, mais fraco que a Besta de Tita. Aqui a
 * horda ja manda lacaios que atiram, nao so perseguem.
 */
public class BossMarteScreen implements Screen {
    private final Game game;
    private final GameSaveData saveData;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final DialogSystem dialog = new DialogSystem();

    private final Rectangle player = new Rectangle(100, 100, 32, 48);
    private final BossMarte boss;
    private final Array<Enemy> minions = new Array<>();
    private final Array<SlashWave> slashesJogador = new Array<>();
    private final Array<SlashWave> pedrasBoss = new Array<>();
    private final Array<SlashWave> tirosMinions = new Array<>();
    private final Array<ItemDrop> dropsO2 = new Array<>();
    private final Array<FoodDrop> comidas = new Array<>();

    private Texture fundoTex, slashTex, bossTex, rochaTex, portraitBoss, alienTex, o2Tex, foodTex, corpseTex;
    private final RecoverySystems.Drone drone = new RecoverySystems.Drone();

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private Animation<TextureRegion> animIdleUnarmed, animWalkUnarmed, animPunch;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;
    private float somEnemyTimer = 0f;

    private float hordeTimer = 0f, cooldown = 0f, reloadTimer = 0f, avisoTimer = 0f, bulletTimer = 0f, chargeTimer = 0f;
    private String mensagemAviso = "";
    private boolean isReloading = false;

    private float fadeAlpha = 1.0f;
    private boolean fadingOut = false;
    private Screen nextScreen = null;

    public BossMarteScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        camera.setToOrtho(false, 800, 600);
        saveData.sincronizarInventario();
        drone.ativo = saveData.droneAtivo;
        drone.loadSprite();
        saveData.codex.visitar("MARTE");
        GameHud.reset();

        boss = new BossMarte(750, 750, BossBalance.hpFor(saveData, 210));

        carregarTexturas();
        spawnAmbientObjects();
                SoundManager.playSound("bossgrowl");

        dialog.start(new String[]{
            "Outro invasor na poeira vermelha...",
            "Eu sou o Titan-Ferrugem. Voce nao vai sair vivo desta tempestade!"
        }, portraitBoss != null ? portraitBoss : bossTex);
    }

    private void carregarTexturas() {
        fundoTex = safeLoad("fundo_marte.png");
        bossTex = safeLoad("boss_marte.png");
        alienTex = safeLoad("alien.png");
        slashTex = safeLoad("slash_wave.png");
        rochaTex = safeLoad("rocha_boss.png");
        portraitBoss = safeLoad("portrait_boss.png");
        o2Tex = safeLoad("o2.png");
        foodTex = safeLoad("food.png");
        corpseTex = safeLoad("cadaver.png");

        Texture idleTex = safeLoad("player_lunar.png");
        Texture walkTex = safeLoad("player_lunar_walk.png");
        Texture slashTexAnim = safeLoad("player_lunar_slash.png");
        Texture idleUnarmedTex = safeLoad("player_unarmed.png");
        Texture walkUnarmedTex = safeLoad("player_unarmed_walk.png");
        Texture punchTex = safeLoad("player_unarmed_punch.png");

        if (idleTex != null) animIdle = new Animation<>(0.2f, TextureRegion.split(idleTex, Math.max(1, idleTex.getWidth() / 4), idleTex.getHeight())[0]);
        if (walkTex != null) animWalk = new Animation<>(0.12f, TextureRegion.split(walkTex, Math.max(1, walkTex.getWidth() / 4), walkTex.getHeight())[0]);
        if (slashTexAnim != null) animSlash = new Animation<>(0.1f, TextureRegion.split(slashTexAnim, Math.max(1, slashTexAnim.getWidth() / 4), slashTexAnim.getHeight())[0]);
        if (idleUnarmedTex != null) animIdleUnarmed = new Animation<>(0.2f, TextureRegion.split(idleUnarmedTex, Math.max(1, idleUnarmedTex.getWidth() / 4), idleUnarmedTex.getHeight())[0]);
        if (walkUnarmedTex != null) animWalkUnarmed = new Animation<>(0.12f, TextureRegion.split(walkUnarmedTex, Math.max(1, walkUnarmedTex.getWidth() / 4), walkUnarmedTex.getHeight())[0]);
        if (punchTex != null) { TextureRegion[][] punchFrames = TextureRegion.split(punchTex, Math.max(1, punchTex.getWidth() / 4), punchTex.getHeight()); animPunch = new Animation<>(0.08f, punchFrames[0][0]); }
    }

    private void spawnAmbientObjects() {
        for (int i = 0; i < 9; i++) {
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
        Gdx.gl.glClearColor(0.35f, 0.14f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, 1200, 1200);

        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        for (ItemDrop d : dropsO2) if (o2Tex != null) batch.draw(o2Tex, d.rect.x, d.rect.y, d.rect.width, d.rect.height);
        for (Enemy m : minions) if (m.ativo && alienTex != null) batch.draw(alienTex, m.rect.x, m.rect.y, m.rect.width, m.rect.height);
        if (boss.ativo && bossTex != null) batch.draw(bossTex, boss.rect.x, boss.rect.y, boss.rect.width, boss.rect.height);

        for (SlashWave s : slashesJogador) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        for (SlashWave p : pedrasBoss) if (p.active && rochaTex != null) batch.draw(rochaTex, p.rect.x, p.rect.y, 32, 32, 64, 64, 1.4f, 1.4f, p.angle, 0, 0, rochaTex.getWidth(), rochaTex.getHeight(), false, false);
        for (SlashWave t : tirosMinions) if (t.active && slashTex != null) batch.draw(slashTex, t.rect.x, t.rect.y, 16, 16, 32, 32, 0.8f, 0.8f, t.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);

        TextureRegion frameAtual = null;
        boolean playerArmado = saveData.inventario != null && saveData.inventario.temArma;
        if (estadoJogador == 2) {
            if (playerArmado && animSlash != null) frameAtual = animSlash.getKeyFrame(stateTime, false);
            else if (!playerArmado && animPunch != null) frameAtual = animPunch.getKeyFrame(stateTime, false);
            else if (!playerArmado && animIdleUnarmed != null) frameAtual = animIdleUnarmed.getKeyFrame(stateTime, true);
        } else if (estadoJogador == 1) {
            if (playerArmado && animWalk != null) frameAtual = animWalk.getKeyFrame(stateTime, true);
            else if (!playerArmado && animWalkUnarmed != null) frameAtual = animWalkUnarmed.getKeyFrame(stateTime, true);
        } else {
            if (playerArmado && animIdle != null) frameAtual = animIdle.getKeyFrame(stateTime, true);
            else if (!playerArmado && animIdleUnarmed != null) frameAtual = animIdleUnarmed.getKeyFrame(stateTime, true);
        }
        if (frameAtual != null) { batch.setColor(chargeTimer > 0 ? new com.badlogic.gdx.graphics.Color(1f,1f,1f,0.55f+0.45f*(float)Math.abs(Math.sin(stateTime*18f))) : com.badlogic.gdx.graphics.Color.WHITE); batch.draw(frameAtual, player.x, player.y, player.width, player.height); batch.setColor(com.badlogic.gdx.graphics.Color.WHITE); }
        if (saveData.cadaverAtivo && corpseTex != null) batch.draw(corpseTex, saveData.cadaverX, saveData.cadaverY, 40, 40);
        drone.draw(batch);
        batch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        drone.drawBeam(shapeRenderer);
        PlayerCombat.drawChargeParticles(shapeRenderer, player, chargeTimer, stateTime);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (boss.ativo && bossTex == null) boss.renderFallback(shapeRenderer);
        for (Enemy m : minions) {
            if (m.ativo) {
                shapeRenderer.setColor(0.8f, 0f, 0f, 1);
                shapeRenderer.rect(m.rect.x, m.rect.y + m.rect.height + 5, m.rect.width, 5);
                shapeRenderer.setColor(0f, 0.8f, 0f, 1);
                shapeRenderer.rect(m.rect.x, m.rect.y + m.rect.height + 5, m.rect.width * Math.max(0, m.hp / (float) m.maxHp), 5);
            }
        }
        shapeRenderer.end();

        desenharHUD();
        desenharFade(delta);

        if (saveData.inventario != null && saveData.inventario.aberto) {
            saveData.inventario.render(batch, shapeRenderer, font, batch.getProjectionMatrix(), saveData);
        }

        CrosshairUtil.desenharMira(shapeRenderer);
    }

    private void update(float delta) {
        if (fadingOut) return;

        if (saveData.inventario != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) saveData.inventario.aberto = !saveData.inventario.aberto;
            if (saveData.inventario.aberto) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.C)) saveData.inventario.usarComida(saveData);
                return;
            }
        }
        if (GameHud.handleInput(saveData)) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && saveData.inventario.drone > 0) { drone.ativo = !drone.ativo; saveData.droneAtivo = drone.ativo; saveData.salvar(); }
        if (saveData.cadaverAtivo && player.overlaps(new Rectangle(saveData.cadaverX, saveData.cadaverY, 50, 50)) && Gdx.input.isKeyJustPressed(Input.Keys.E)) { RecoverySystems.recuperarCadaver(saveData); return; }

        if (somEnemyTimer > 0f) somEnemyTimer -= delta;
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;
        RecoverySystems.updateOxygen(saveData, delta, 0.65f);
        if (saveData.vida <= 0) { RecoverySystems.criarCadaver(saveData, player.x, player.y); saveData.fase = "MARTE_BOSS"; saveData.salvar(); game.setScreen(new GameOverScreen(game)); return; }

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
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { dx -= 280 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { dx += 280 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { dy += 280 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { dy -= 280 * delta; moving = true; }
        WorldCollision.movePlayer(player, dx, dy, new Array<>(), 1200f, 1200f);
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

        for (int i = comidas.size - 1; i >= 0; i--) {
            FoodDrop food = comidas.get(i);
            if (player.overlaps(food.rect)) {
                saveData.inventario.add("COMIDA");
                comidas.removeIndex(i);
                mensagemAviso = "+1 COMIDA! Aperte I e depois C para usar.";
                avisoTimer = 2f;
                SoundManager.playSound("pickup");
            }
        }

        if (saveData.inventario.temArma && Gdx.input.isKeyJustPressed(Input.Keys.R) && !isReloading && saveData.municao < 25) {
            isReloading = true; reloadTimer = 1.5f; mensagemAviso = "RECARREGANDO..."; avisoTimer = 1.5f;
            SoundManager.playSound("reload");
        }
        if (isReloading) {
            reloadTimer -= delta;
            if (reloadTimer <= 0) { isReloading = false; saveData.municao = 25; mensagemAviso = "MUNICÃO RECARREGADA!"; avisoTimer = 2f; }
        }

        // Horda de lacaios que atiram — mais frequente que na Lua, refletindo a progressao.
        if (boss.ativo) {
            hordeTimer += delta;
            if (hordeTimer >= 10f) {
                hordeTimer = 0f;
                mensagemAviso = "O TITAN-FERRUGEM CONVOCOU UMA HORDA!";
                avisoTimer = 2.5f;
                int amount = RouteSystem.enemyCount(saveData, 2);
                for (int i = 0; i < amount; i++) minions.add(new Enemy(boss.rect.x + 60 + i * 42f, boss.rect.y + (i % 2) * 28f, 1));
                if (somEnemyTimer <= 0) { SoundManager.playSound("enemy"); somEnemyTimer = 25f; }
            }
        }

        for (int i = minions.size - 1; i >= 0; i--) {
            Enemy m = minions.get(i);
            if (m.ativo) {
                m.update(delta, new Vector2(player.x, player.y));
                if (m.cooldownTiro <= 0 && new Vector2(player.x - m.rect.x, player.y - m.rect.y).len() < 500f) {
                    tirosMinions.add(new SlashWave(m.rect.x, m.rect.y, player.x, player.y));
                    m.cooldownTiro = 2.5f;
                }
                if (m.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, (RouteSystem.isAggressive(saveData) ? 13f : 10f) * delta);
            } else {
                minions.removeIndex(i);
            }
        }

        boss.update(delta, new Vector2(player.x, player.y));
        if (drone.ativo) {
            boolean disparouDrone = drone.assist(delta, player.x, player.y, saveData, boss.rect.x + boss.rect.width * 0.5f, boss.rect.y + boss.rect.height * 0.5f, boss.ativo);
            if (disparouDrone && boss.ativo) {
                boss.levarDano(Math.max(1, Math.round(UpgradeSystem.danoArma(saveData) * 0.45f)));
                SoundManager.playSound("hit_enemy");
                if (!boss.ativo) { saveData.bossMarteDerrotado = true; saveData.inventario.add("CHAVE_MARTE"); saveData.fase = "TITA"; saveData.salvar(); fadingOut = true; nextScreen = new TitanScreen(game, saveData); }
            }
        }
        if (boss.ativo && boss.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, boss.getContactDamage() * delta);
        if (boss.consumeRockThrow()) {
            pedrasBoss.add(new SlashWave(boss.rect.x + boss.rect.width / 2f, boss.rect.y + boss.rect.height / 2f, boss.getRockTarget().x, boss.getRockTarget().y));
        }
        bulletTimer += delta;
        if (boss.ativo && bulletTimer >= (RouteSystem.isAggressive(saveData) ? 2.6f : 3.6f)) {
            bulletTimer = 0f;
            float cx=boss.rect.x+boss.rect.width/2f, cy=boss.rect.y+boss.rect.height/2f;
            int n=RouteSystem.isAggressive(saveData)?10:8;
            for(int i=0;i<n;i++){ float a=stateTime*35f+i*(360f/n); float tx=cx+MathUtils.cosDeg(a)*620f, ty=cy+MathUtils.sinDeg(a)*620f; tirosMinions.add(new SlashWave(cx,cy,tx,ty)); }
        }
        if (boss.ativo && bulletTimer > 2.3f && bulletTimer < 2.34f) {
            float tx=player.x+MathUtils.cos(stateTime)*120f, ty=player.y+MathUtils.sin(stateTime)*120f;
            tirosMinions.add(new SlashWave(boss.rect.x,boss.rect.y,tx,ty));
        }

        if (!saveData.inventario.temArma && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && cooldown <= 0f) {
            socarMarte();
        } else if (saveData.inventario.temArma && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && saveData.municao > 0 && !isReloading) {
            chargeTimer = Math.min(1.2f, chargeTimer + delta);
        } else if (saveData.inventario.temArma && chargeTimer >= 0.8f && saveData.municao > 0 && !isReloading) {
            dispararMarte(true); chargeTimer = 0f;
        } else if (saveData.inventario.temArma && chargeTimer > 0f && saveData.municao > 0 && !isReloading) {
            dispararMarte(false); chargeTimer = 0f;
        }
        if (saveData.inventario.temArma && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && saveData.municao > 0 && cooldown <= 0f && !isReloading) dispararMarte(false);

        for (int i = slashesJogador.size - 1; i >= 0; i--) {
            SlashWave s = slashesJogador.get(i); s.update(delta);
            if (!s.active) { slashesJogador.removeIndex(i); continue; }

            if (boss.ativo && s.rect.overlaps(boss.rect)) {
                boss.levarDano(UpgradeSystem.danoArma(saveData)); s.active = false;
                SoundManager.playSound("hit_enemy");
                if (!boss.ativo) {
                    saveData.bossMarteDerrotado = true;
                    saveData.inventario.add("CHAVE_MARTE");
                    saveData.inventario.municao = 25; saveData.municao = 25;
                    saveData.fase = "TITA"; saveData.titaMissoesOk = true;
                    saveData.salvar();
                    fadingOut = true; nextScreen = new TitanScreen(game, saveData);
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
                        if (MathUtils.randomBoolean(0.5f)) dropsO2.add(new ItemDrop(m.rect.x, m.rect.y, 0));
                        if (saveData.inventario != null && MathUtils.randomBoolean(0.3f)) saveData.inventario.add("COMIDA");
                    }
                    break;
                }
            }
        }

        for (int i = pedrasBoss.size - 1; i >= 0; i--) {
            SlashWave p = pedrasBoss.get(i);
            p.update(delta);
            if (p.rect.overlaps(player)) { UpgradeSystem.aplicarDano(saveData, boss.getRockDamage()); p.active = false; }
            if (!p.active) pedrasBoss.removeIndex(i);
        }

        for (int i = tirosMinions.size - 1; i >= 0; i--) {
            SlashWave t = tirosMinions.get(i);
            t.update(delta);
            if (t.rect.overlaps(player)) { UpgradeSystem.aplicarDano(saveData, 10f); t.active = false; }
            if (!t.active) tirosMinions.removeIndex(i);
        }
    }

    private void dispararMarte(boolean charged) { estadoJogador=2; slashAnimTimer=.3f; stateTime=0f; saveData.municao--; saveData.inventario.municao=saveData.municao; cooldown=Math.max(.14f,.25f-.02f*saveData.inventario.nivelArma); SoundManager.playSound(charged ? "charged" : "slash"); Vector3 m=new Vector3(Gdx.input.getX(),Gdx.input.getY(),0); camera.unproject(m); slashesJogador.add(new SlashWave(player.x,player.y,m.x,m.y,charged)); }

    private void desenharHUD() {
        GameHud.drawBoss(shapeRenderer, batch, font, saveData, "MARTE",
                avisoTimer > 0 ? mensagemAviso : "", "TITAN-FERRUGEM",
                boss.hp, boss.maxHp, 1, 1, boss.ativo);
        if (!GameHud.isLogAberto()) dialog.render(batch, shapeRenderer, font);
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
            if (!fadingOut && fadeAlpha > 0f) LoadingOverlay.draw(shapeRenderer, stateTime, fadeAlpha);

            if (fadingOut && fadeAlpha >= 1.0f && nextScreen != null) {
                game.setScreen(nextScreen);
            }
        }
    }


    private void socarMarte() {
        Vector3 m=new Vector3(Gdx.input.getX(),Gdx.input.getY(),0); camera.unproject(m);
        Rectangle hit=PlayerCombat.punchBox(player,m); estadoJogador=2; slashAnimTimer=.28f; stateTime=0f; cooldown=.28f; SoundManager.playSound("punch");
        if (boss.ativo && hit.overlaps(boss.rect)) { boss.levarDano(8); SoundManager.playSound("hit_enemy"); if (!boss.ativo) { saveData.bossMarteDerrotado=true; saveData.inventario.add("CHAVE_MARTE"); saveData.fase="TITA"; saveData.salvar(); fadingOut=true; nextScreen=new TitanScreen(game,saveData); } }
    }

    @Override public void show() { Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None); SoundManager.playMusic("boss", true); } @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {}
    @Override public void hide() { SoundManager.stopMusic(); GameHud.reset(); }
    @Override public void dispose() { drone.dispose(); batch.dispose(); shapeRenderer.dispose(); font.dispose(); }
}
