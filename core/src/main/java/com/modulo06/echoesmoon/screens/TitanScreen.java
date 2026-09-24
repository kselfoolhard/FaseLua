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
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.WorldRock;
import com.modulo06.echoesmoon.entities.ItemDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.BossBalance;
import com.modulo06.echoesmoon.systems.CrosshairUtil;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SegredoSystem;
import com.modulo06.echoesmoon.systems.SoundManager;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;
import com.modulo06.echoesmoon.systems.PlayerCombat;
import com.modulo06.echoesmoon.systems.GameHud;
import com.modulo06.echoesmoon.systems.LoadingOverlay;
import com.modulo06.echoesmoon.systems.RouteSystem;
import com.modulo06.echoesmoon.systems.RecoverySystems;

public class TitanScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Rectangle player;
    private Rectangle segredoTita;
    private Enemy boss;
    private Array<Enemy> minions;
    private Array<SlashWave> slashesJogador;
    private Array<SlashWave> pedrasBoss;
    private Array<SlashWave> tirosMinions;
    private Array<ItemDrop> dropsO2;
    private Array<FoodDrop> comidas;
    private Array<WorldRock> pedrasMapa;
    private DialogSystem dialog;

    private Texture fundoTex, slashTex, bossTex, rochaTex, portraitBoss, alienTex, o2Tex, foodTex, iceTex, pedraTex, corpseTex;
    private final RecoverySystems.Drone drone = new RecoverySystems.Drone();

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private Animation<TextureRegion> animIdleUnarmed, animWalkUnarmed, animPunch;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;
    private float somEnemyTimer = 0f;


    private int bossState = 0;
    private boolean lanternaLigada = false;
    private boolean bossEscondido = true;
    private float bossFugaTimer = 0f;
    private int bossHits = 0;
    private float danoDesdeTeleporte = 0f;
    private float chargeTimer = 0f;
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
        segredoTita = new Rectangle(1120, 1120, 40, 40);

        boss = new Enemy(800, 800, 0);
        boss.rect.width = 110;
        boss.rect.height = 110;
        boss.maxHp = BossBalance.hpFor(saveData, 300);
        boss.hp = boss.maxHp;

        minions = new Array<>();
        slashesJogador = new Array<>();
        pedrasBoss = new Array<>();
        tirosMinions = new Array<>();
        dropsO2 = new Array<>();
        comidas = new Array<>();
        pedrasMapa = new Array<>();
        saveData.sincronizarInventario();
        drone.ativo = saveData.droneAtivo;
        drone.loadSprite();
        saveData.codex.visitar("TITA");
        GameHud.reset();

        carregarTexturas();
        spawnAmbientObjects();
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
        iceTex = safeLoad("ice.png");
        pedraTex = safeLoad("pedra.png");
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
        Array<Rectangle> forbidden = new Array<>();
        forbidden.add(player);
        forbidden.add(boss.rect);
        for (FoodDrop food : comidas) forbidden.add(food.rect);
        for (int i = 0; i < 12; i++) {
            float x = MathUtils.random(70f, 1120f);
            float y = MathUtils.random(70f, 1120f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (r.overlaps(player) || r.overlaps(boss.rect)) continue;
            comidas.add(new FoodDrop(x, y));
        }
        WorldRock.spawnMany(pedrasMapa, 20, 1200f, 1200f, player, forbidden, 130f, 303L);
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

        if (iceTex != null && !saveData.segredoTitaEncontrado) {
            batch.setColor(1f, 1f, 1f, 0.22f);
            batch.draw(iceTex, segredoTita.x, segredoTita.y, segredoTita.width, segredoTita.height);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        for (WorldRock rock : pedrasMapa) if (pedraTex != null) batch.draw(pedraTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height);
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        for (ItemDrop d : dropsO2) if (o2Tex != null) batch.draw(o2Tex, d.rect.x, d.rect.y, d.rect.width, d.rect.height);
        for (Enemy m : minions) if (m.ativo && alienTex != null) batch.draw(alienTex, m.rect.x, m.rect.y, m.rect.width, m.rect.height);
        // O Titã permanece 100% visível durante toda a luta.
        if (boss.ativo) {
            if (bossTex != null) batch.draw(bossTex, boss.rect.x, boss.rect.y, boss.rect.width, boss.rect.height);
        }

        for (SlashWave s : slashesJogador) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        for (SlashWave p : pedrasBoss) if (p.active && rochaTex != null) batch.draw(rochaTex, p.rect.x, p.rect.y, 32, 32, 64, 64, 1.5f, 1.5f, p.angle, 0, 0, rochaTex.getWidth(), rochaTex.getHeight(), false, false);
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
        drone.draw(batch);
        if (saveData.cadaverAtivo && corpseTex != null) batch.draw(corpseTex, saveData.cadaverX, saveData.cadaverY, 40, 40);
        batch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        drone.drawBeam(shapeRenderer);
        PlayerCombat.drawChargeParticles(shapeRenderer, player, chargeTimer, stateTime);

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

        // Tita fica escura, mas nunca completamente preta. A lanterna acompanha a crosshair.
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float left = camera.position.x - 400f, bottom = camera.position.y - 300f;

        // Escurecimento global: suficiente para criar clima sem esconder o cenario.
        shapeRenderer.setColor(0.01f, 0.014f, 0.024f, lanternaLigada ? 0.34f : 0.48f);
        shapeRenderer.rect(left, bottom, 800f, 600f);

        if (lanternaLigada) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouse);

            float cx = player.x + player.width * 0.5f;
            float cy = player.y + player.height * 0.5f;
            Vector2 dir = new Vector2(mouse.x - cx, mouse.y - cy);
            if (dir.isZero()) dir.set(1f, 0f);
            dir.nor();

            // Feixe acompanha exatamente a direcao da crosshair.
            Vector2 side = dir.cpy().rotateDeg(90f).scl(105f);
            float reach = 440f;
            shapeRenderer.setColor(1f, 0.94f, 0.66f, 0.24f);
            shapeRenderer.triangle(
                cx, cy,
                cx + dir.x * reach + side.x, cy + dir.y * reach + side.y,
                cx + dir.x * reach - side.x, cy + dir.y * reach - side.y);

            // Halo perto do jogador para ele continuar visivel.
            shapeRenderer.setColor(1f, 0.98f, 0.82f, 0.20f);
            shapeRenderer.circle(cx, cy, 92f);

            // Nucleo mais forte perto do ponto para onde a lanterna aponta.
            shapeRenderer.setColor(1f, 0.98f, 0.86f, 0.12f);
            shapeRenderer.circle(cx + dir.x * 120f, cy + dir.y * 120f, 86f);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        desenharHUD();
        desenharFade(delta);

        // --- SISTEMA DE INVENTARIO: RENDERIZA POR CIMA DE TUDO ---
        if (saveData.inventario != null && saveData.inventario.aberto) {
            saveData.inventario.render(batch, shapeRenderer, font, batch.getProjectionMatrix(), saveData);
        }

        CrosshairUtil.desenharMira(shapeRenderer);
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && saveData.inventario.drone > 0) { drone.ativo = !drone.ativo; saveData.droneAtivo = drone.ativo; saveData.salvar(); }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            lanternaLigada = !lanternaLigada;
            mensagemAviso = lanternaLigada ? "LANTERNA LIGADA" : "LANTERNA DESLIGADA";
            avisoTimer = 1.5f;
            // A lanterna não esconde mais o Titã. Ela apenas pode causar dano quando mirada nele.
            bossEscondido = false;
        }

        if (somEnemyTimer > 0f) somEnemyTimer -= delta;
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;
        if (bossFugaTimer > 0f) bossFugaTimer -= delta;
        RecoverySystems.updateOxygen(saveData, delta, 0.8f);
        if (saveData.vida <= 0) { RecoverySystems.criarCadaver(saveData, player.x, player.y); saveData.fase = "TITA"; saveData.salvar(); game.setScreen(new GameOverScreen(game)); return; }

        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) dialog.next();
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !saveData.segredoTitaEncontrado) {
            Vector3 cliqueMundo = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(cliqueMundo);
            if (SegredoSystem.checarClique(saveData, SegredoSystem.TITA, segredoTita, cliqueMundo.x, cliqueMundo.y)) {
                mensagemAviso = "Voce sentiu algo estranho sob o gelo...";
                avisoTimer = 2.5f;
                SoundManager.playSound("pickup");
            }
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

        if (saveData.inventario.temArma && Gdx.input.isKeyJustPressed(Input.Keys.R) && !isReloading && saveData.municao < 25) {
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
                if (m.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, (RouteSystem.isAggressive(saveData) ? 13f : 10f) * delta);
            } else {
                minions.removeIndex(i);
            }
        }

        // O Titã nunca fica escondido. A lanterna, quando apontada diretamente para ele,
        // também causa dano continuamente.
        bossEscondido = false;

        Vector3 mouseLanterna = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseLanterna);
        float cxLanterna = player.x + player.width * 0.5f;
        float cyLanterna = player.y + player.height * 0.5f;
        Vector2 lanternaDir = new Vector2(mouseLanterna.x - cxLanterna, mouseLanterna.y - cyLanterna);
        Vector2 bossDir = new Vector2(
            boss.rect.x + boss.rect.width * 0.5f - cxLanterna,
            boss.rect.y + boss.rect.height * 0.5f - cyLanterna);
        float distanciaLanterna = bossDir.len();
        float dotLanterna = lanternaDir.isZero() || bossDir.isZero() ? 1f : lanternaDir.cpy().nor().dot(bossDir.cpy().nor());

        if (boss.ativo && lanternaLigada && distanciaLanterna <= 420f && dotLanterna > 0.55f) {
            // Dano da lanterna: 30 por segundo enquanto estiver mirando no Titã.
            aplicarDanoBoss(30f * delta);
        }

        if (boss.ativo && drone.assist(delta, player.x, player.y, saveData,
            boss.rect.x + boss.rect.width * 0.5f,
            boss.rect.y + boss.rect.height * 0.5f, true)) {
            aplicarDanoBoss(Math.max(4f, UpgradeSystem.danoArma(saveData) * 0.45f));
            SoundManager.playSound("hit_enemy");
        } else if (drone.ativo) {
            // Mesmo sem alvo visivel, o drone continua recuperando O2.
            drone.assist(delta, player.x, player.y, saveData, player.x, player.y, false);
        }

        if (boss.ativo) {
            bossTimer += delta;
            hordeTimer += delta;

            if (hordeTimer >= 9f) {
                hordeTimer = 0f;
                mensagemAviso = "O CHEFE CONVOCOU UMA HORDA!";
                avisoTimer = 2.5f;
                int amount = RouteSystem.enemyCount(saveData, 2);
                for (int i = 0; i < amount; i++) minions.add(new Enemy(boss.rect.x + 60 + i * 42f, boss.rect.y + (i % 2) * 28f, 1));
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
            } else if (bossState == 1) { if (bossTimer > 0.8f) { pedrasBoss.add(new SlashWave(boss.rect.x + boss.rect.width/2, boss.rect.y + boss.rect.height/2, player.x, player.y)); bossState = 0; bossTimer = 0f; } }
            if (boss.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, 12f * delta);
        }

        if (!saveData.inventario.temArma && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && cooldown <= 0f) {
            socarTita();
        } else if (saveData.inventario.temArma && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && saveData.municao > 0 && !isReloading) {
            chargeTimer = Math.min(1.2f, chargeTimer + delta);
        } else if (chargeTimer >= 0.8f && saveData.municao > 0 && !isReloading) {
            dispararTita(true); chargeTimer = 0f;
        } else if (chargeTimer > 0f && saveData.municao > 0 && !isReloading) {
            dispararTita(false); chargeTimer = 0f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && saveData.municao > 0 && cooldown <= 0f && !isReloading) dispararTita(false);

        for (int i = slashesJogador.size - 1; i >= 0; i--) {
            SlashWave s = slashesJogador.get(i); s.update(delta);
            if (!s.active) { slashesJogador.removeIndex(i); continue; }

            if (boss.ativo && s.rect.overlaps(boss.rect)) {
                aplicarDanoBoss(UpgradeSystem.danoArma(saveData) * s.damageMultiplier);
                s.active = false;
                SoundManager.playSound("hit_enemy");
                bossHits++;
                continue;
            }

            for (Enemy m : minions) {
                if (m.ativo && s.rect.overlaps(m.rect)) {
                    m.hp -= UpgradeSystem.danoArma(saveData); s.active = false; SoundManager.playSound("hit_enemy");
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


    /**
     * Aplica dano ao Titã e controla o teleporte a cada 100 de dano acumulado.
     * O contador é zerado somente quando o teleporte acontece.
     */
    private void aplicarDanoBoss(float dano) {
        if (!boss.ativo || dano <= 0f) return;

        boss.hp -= dano;
        danoDesdeTeleporte += dano;

        if (boss.hp <= 0f) {
            boss.ativo = false;
            saveData.bossTitaDerrotado = true;
            saveData.inventario.add("CHAVE_TITA");
            saveData.fase = "CALISTO";
            saveData.salvar();
            fadingOut = true;
            nextScreen = new CallistoScreen(game, saveData);
            return;
        }

        // Só teleporta depois de receber 100 de dano desde o último teleporte.
        if (danoDesdeTeleporte >= 100f) {
            danoDesdeTeleporte = 0f;
            boss.rect.x = MathUtils.random(140, 1040);
            boss.rect.y = MathUtils.random(160, 1040);
            bossState = 0;
            bossTimer = 0f;
            SoundManager.playSound("bossgrowl");
            mensagemAviso = "O TITÃ SE TELEPORTOU!";
            avisoTimer = 1.5f;
        }
    }

    private void socarTita() {
        Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(m);
        Rectangle hit = PlayerCombat.punchBox(player, m);
        estadoJogador = 2;
        slashAnimTimer = .28f;
        stateTime = 0f;
        cooldown = .28f;
        SoundManager.playSound("punch");

        if (boss.ativo && hit.overlaps(boss.rect)) {
            aplicarDanoBoss(8f);
            bossHits++;
            SoundManager.playSound("hit_enemy");
        }
    }

    private void dispararTita(boolean charged) {
        estadoJogador=2; slashAnimTimer=.3f; stateTime=0f; saveData.municao--; saveData.inventario.municao=saveData.municao; cooldown=.18f; SoundManager.playSound(charged ? "charged" : "slash"); Vector3 m=new Vector3(Gdx.input.getX(),Gdx.input.getY(),0); camera.unproject(m); slashesJogador.add(new SlashWave(player.x,player.y,m.x,m.y,charged));
    }

    private void desenharHUD() {
        GameHud.drawBoss(shapeRenderer, batch, font, saveData, "TITA",
            avisoTimer > 0 ? mensagemAviso : "", "BESTA DE TITA",
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

    @Override public void show() { Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None); SoundManager.playMusic("tita", true); } @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {}

    @Override
    public void hide() { SoundManager.stopMusic(); GameHud.reset(); }

    @Override
    public void dispose() { drone.dispose(); batch.dispose(); shapeRenderer.dispose(); font.dispose(); }
}

