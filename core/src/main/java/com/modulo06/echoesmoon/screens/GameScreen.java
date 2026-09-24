package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.WorldRock;
import com.modulo06.echoesmoon.entities.ItemDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.CrosshairUtil;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SegredoSystem;
import com.modulo06.echoesmoon.systems.SoundManager;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;
import com.modulo06.echoesmoon.systems.PlayerCombat;
import com.modulo06.echoesmoon.systems.RecoverySystems;
import com.modulo06.echoesmoon.systems.GameHud;
import com.modulo06.echoesmoon.systems.LoadingOverlay;
import com.modulo06.echoesmoon.systems.RouteSystem;

public class GameScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture fundoLuaTex, portalTex, slashTex, bancadaTex, alienTex, portraitOficial, npcTex, baseTex, caixaTex, foodTex, iceTex, pedraTex, corpseTex, alavancaTex;

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private Animation<TextureRegion> animIdleUnarmed, animWalkUnarmed, animPunch;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;
    private float somEnemyTimer = 0f;

    private Rectangle player, portalParaMarte, bancada, zonaInteracao, npcLua, safeZone;
    private Rectangle segredoLua;
    private Array<SlashWave> slashes;
    private Array<Enemy> enemies;
    private Array<ItemDrop> caixasCrafting;
    private Array<FoodDrop> comidas;
    private Array<WorldRock> pedras;
    private DialogSystem dialog;

    private final float WORLD_WIDTH = 1200f;
    private final float WORLD_HEIGHT = 1200f;

    private float cooldown = 0f, avisoTimer = 0f, reloadTimer = 0f, chargeTimer = 0f;
    private float craftingProgress = 0f;
    private String mensagemAviso = "";
    private boolean falouOficial = false;
    private boolean caixasSpawnadas = false;
    private boolean armaCraftada = false;
    private boolean isReloading = false;
    private boolean mostrarMapa = false;
    private boolean tempestade = false;
    private boolean mostrarCraft = false;
    private final RecoverySystems.Drone drone = new RecoverySystems.Drone();
    private final Rectangle[] alavancas = {new Rectangle(520,760,34,60),new Rectangle(650,760,34,60),new Rectangle(780,760,34,60)};
    private final int[] ordemAlavancas = {2,0,1};
    private int proximaAlavanca = 0;

    private int caixasColetadas = 0;
    private final int CAIXAS_NECESSARIAS = 3;

    private float fadeAlpha = 1.0f;
    private boolean fadingOut = false;
    private Screen nextScreen = null;

    public GameScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        dialog = new DialogSystem();

        player = new Rectangle(saveData.playerX, saveData.playerY, 32, 48);
        portalParaMarte = new Rectangle(1000, 1000, 100, 100);
        bancada = new Rectangle(500, 400, 64, 64);
        zonaInteracao = new Rectangle(bancada.x - 20, bancada.y - 20, bancada.width + 40, bancada.height + 40);

        npcLua = new Rectangle(250, 350, 36, 54);
        safeZone = new Rectangle(180, 280, 160, 160);
        // Bancada escondida (disfarcada de gelo) da rota estranha — bem no cantinho do mapa.
        segredoLua = new Rectangle(1120, 60, 40, 40);

        this.saveData.sincronizarInventario();
        saveData.codex.visitar("LUA");
        if (!saveData.receitaCraftada && saveData.inventario.gelo == 0) saveData.inventario.add("GELO");
        if (!saveData.receitaCraftada && saveData.inventario.peca == 0) saveData.inventario.add("PECA");
        if (saveData.inventario.drone == 0) saveData.inventario.add("DRONE");
        drone.ativo = saveData.droneAtivo;
        drone.loadSprite();
        this.armaCraftada = this.saveData.inventario.temArma;
        this.falouOficial = this.saveData.luaMissoesOk;
        slashes = new Array<>();
        enemies = new Array<>();
        caixasCrafting = new Array<>();
        comidas = new Array<>();
        pedras = new Array<>();

        carregarTexturas();
        spawnAmbientObjects();
            }

    private void carregarTexturas() {
        fundoLuaTex = safeLoad("fundo.png");
        portalTex = safeLoad("portal.png");
        slashTex = safeLoad("slash_wave.png");
        bancadaTex = safeLoad("bancada.png");
        alienTex = safeLoad("alien_lunar.png");
        portraitOficial = safeLoad("portrait_oficial.png");
        npcTex = safeLoad("npc.png");
        baseTex = safeLoad("base.png");
        caixaTex = safeLoad("item.png");
        foodTex = safeLoad("food.png");
        iceTex = safeLoad("ice.png");
        pedraTex = safeLoad("pedra.png");
        corpseTex = safeLoad("cadaver.png"); alavancaTex = safeLoad("alavanca.png");

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

    private Texture safeLoad(String path) {
        try { if (Gdx.files.internal(path).exists()) return new Texture(path); } catch (Exception ignored) {}
        return null;
    }


    private void spawnAmbientObjects() {
        Array<Rectangle> forbidden = new Array<>();
        forbidden.add(safeZone);
        forbidden.add(bancada);
        forbidden.add(npcLua);
        forbidden.add(portalParaMarte);
        for (FoodDrop food : comidas) forbidden.add(food.rect);
        for (int i = 0; i < 10; i++) {
            float x = MathUtils.random(80f, WORLD_WIDTH - 80f);
            float y = MathUtils.random(80f, WORLD_HEIGHT - 80f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (r.overlaps(player) || r.overlaps(safeZone) || r.overlaps(bancada) || r.overlaps(npcLua) || r.overlaps(portalParaMarte)) continue;
            comidas.add(new FoodDrop(x, y));
        }
        WorldRock.spawnMany(pedras, 16, WORLD_WIDTH, WORLD_HEIGHT, player, forbidden, 120f, 101L);
    }

    private void gerarCaixasEInimigos() {
        caixasCrafting.add(new ItemDrop(700, 200, 1));
        caixasCrafting.add(new ItemDrop(900, 600, 1));
        caixasCrafting.add(new ItemDrop(300, 900, 1));

        int enemyCount = RouteSystem.enemyCount(saveData, 5);
        for (int i = 0; i < enemyCount; i++) {
            enemies.add(new Enemy(MathUtils.random(500, 1100), MathUtils.random(500, 1100), 0));
        }

        if (somEnemyTimer <= 0) {
            SoundManager.playSound("enemy");
            somEnemyTimer = 25f;
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (fundoLuaTex != null) batch.draw(fundoLuaTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        if (baseTex != null) batch.draw(baseTex, safeZone.x, safeZone.y, safeZone.width, safeZone.height);
        if (portalTex != null) batch.draw(portalTex, portalParaMarte.x, portalParaMarte.y, portalParaMarte.width, portalParaMarte.height);
        if (bancadaTex != null) batch.draw(bancadaTex, bancada.x, bancada.y, bancada.width, bancada.height);
        if (npcTex != null) batch.draw(npcTex, npcLua.x, npcLua.y, npcLua.width, npcLua.height);

        if (iceTex != null && !saveData.segredoLuaEncontrado) {
            // Bancada escondida: quase invisivel, so uma sombra de gelo no chao.
            batch.setColor(1f, 1f, 1f, 0.22f);
            batch.draw(iceTex, segredoLua.x, segredoLua.y, segredoLua.width, segredoLua.height);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        for (WorldRock rock : pedras) if (pedraTex != null) batch.draw(pedraTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height);
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        for (ItemDrop caixa : caixasCrafting) if (caixaTex != null) batch.draw(caixaTex, caixa.rect.x, caixa.rect.y, caixa.rect.width, caixa.rect.height);
        for (Enemy e : enemies) if (e.ativo && alienTex != null) batch.draw(alienTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        for (SlashWave s : slashes) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        for (Rectangle lever : alavancas) if (alavancaTex != null) batch.draw(alavancaTex, lever.x, lever.y, lever.width, lever.height);
        if (saveData.cadaverAtivo && corpseTex != null) batch.draw(corpseTex, saveData.cadaverX, saveData.cadaverY, 40, 40);

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

        if (frameAtual != null) { if (chargeTimer > 0) batch.setColor(1f,1f,1f,0.55f+0.45f*(float)Math.abs(Math.sin(stateTime*18f))); batch.draw(frameAtual, player.x, player.y, player.width, player.height); batch.setColor(Color.WHITE); }
        drone.draw(batch);

        if (player.overlaps(npcLua) && !dialog.isOpen()) font.draw(batch, "[E] FALAR COM OFICIAL", npcLua.x - 30, npcLua.y + 57);
        if (player.overlaps(zonaInteracao) && craftingProgress == 0 && !dialog.isOpen()) {
            if (!armaCraftada) font.draw(batch, caixasColetadas >= CAIXAS_NECESSARIAS ? "SEGURE [E] PARA CRAFTAR SLASHWAVE" : "COLETE " + caixasColetadas + "/" + CAIXAS_NECESSARIAS + " PEÇAS!", bancada.x - 40, bancada.y - 10);
            else font.draw(batch, "[E] RECARREGAR MUNIÇÃO", bancada.x - 20, bancada.y - 10);
        }

        if (player.overlaps(portalParaMarte)) font.draw(batch, armaCraftada ? "[E] IR PARA A CRATERA" : "PORTAL BLOQUEADO (REQUER SLASHWAVE)", portalParaMarte.x - 20, portalParaMarte.y - 20);
        if (drone.ativo) font.draw(batch, "DRONE ONLINE", player.x - 15, player.y + 70);

        batch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        drone.drawBeam(shapeRenderer);
        PlayerCombat.drawChargeParticles(shapeRenderer, player, chargeTimer, stateTime);

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

        if (craftingProgress > 0) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(bancada.x, bancada.y + bancada.height + 15, bancada.width, 8);
            shapeRenderer.setColor(0.9f, 0.7f, 0.1f, 1f);
            shapeRenderer.rect(bancada.x, bancada.y + bancada.height + 15, bancada.width * (craftingProgress / 1.5f), 8);
        }
        shapeRenderer.end();

        desenharHUD();
        desenharFade(delta);

        // --- SISTEMA DE INVENTARIO: RENDERIZA POR CIMA DE TUDO ---
        if (saveData.inventario != null && saveData.inventario.aberto) {
            saveData.inventario.render(batch, shapeRenderer, font, batch.getProjectionMatrix(), saveData);
        }

        CrosshairUtil.desenharMira(shapeRenderer);
    }

    private void desenharHUD() {
        GameHud.draw(shapeRenderer, batch, font, saveData, "LUA",
                avisoTimer > 0 ? mensagemAviso : "");
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

    private void update(float delta) {
        if (fadingOut) return;

        // --- CONTROLE DO INVENTÁRIO & PAUSA ---
        if (saveData.inventario != null) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) saveData.inventario.aberto = !saveData.inventario.aberto;
            if (saveData.inventario.aberto) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.C)) saveData.inventario.usarComida(saveData);
                return;
            }
        }

        if (GameHud.handleInput(saveData)) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) mostrarMapa = !mostrarMapa;
        if (mostrarMapa && Gdx.input.isKeyJustPressed(Input.Keys.F) && saveData.inventario.chaveMarte) {
            saveData.fase = "MARTE"; saveData.salvar(); game.setScreen(new MarsScreen(game, saveData)); return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && saveData.inventario.drone > 0) {
            drone.ativo = !drone.ativo; saveData.droneAtivo = drone.ativo; saveData.salvar();
        }

        if (somEnemyTimer > 0f) somEnemyTimer -= delta;
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;

        saveData.cicloTempestade += delta;
        if (saveData.cicloTempestade >= 20f) saveData.cicloTempestade = 0f;
        tempestade = saveData.cicloTempestade >= 12f;
        if (player.overlaps(safeZone)) {
            saveData.o2 = Math.min(100f, saveData.o2 + 15f * delta);
            if (Gdx.input.isKeyPressed(Input.Keys.E)) saveData.escudo = saveData.escudoMax;
        }
        else RecoverySystems.updateOxygen(saveData, delta, tempestade ? 4.0f : 0.5f);

        if (saveData.vida <= 0) { RecoverySystems.criarCadaver(saveData, player.x, player.y); saveData.fase="LUA"; saveData.salvar(); game.setScreen(new GameOverScreen(game)); return; }

        if (dialog.isOpen()) {
            if (dialog.aguardandoEscolha()) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                    saveData.rotaA = true;
                    dialog.escolher();
                    saveData.inventario.add("COMIDA");
                    dialog.start(new String[]{
                        "Certo. Voce escolheu preservar a colonia.",
                        "Leve estas racoes e termine o trabalho sem transformar a base em um cemiterio.",
                        "Quando cruzar para Marte, procure o sinal do radio. Eu nao vou conseguir guia-lo de longe."
                    }, portraitOficial);
                    saveData.salvar();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                    saveData.rotaB = true;
                    dialog.escolher();
                    dialog.start(new String[]{
                        "Entao vai ser do seu jeito.",
                        "Mais inimigos vao aparecer a partir de agora. Nao espere uma passagem tranquila.",
                        "Se quer chegar ate o fim pela forca, vai ter que trabalhar muito mais pelos seus upgrades."
                    }, portraitOficial);
                    saveData.salvar();
                }
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                dialog.next();
                if (!dialog.isOpen() && player.overlaps(npcLua)) {
                    falouOficial = true;
                    if (saveData.luaPuzzleConcluido && !caixasSpawnadas) { gerarCaixasEInimigos(); caixasSpawnadas = true; }
                }
            }
            return;
        }

        if (!saveData.luaPuzzleConcluido) {
            for (int i=0;i<alavancas.length;i++) {
                if (player.overlaps(alavancas[i]) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    if (ordemAlavancas[proximaAlavanca] == i) {
                        proximaAlavanca++; mensagemAviso = "ALAVANCAS " + proximaAlavanca + "/3"; avisoTimer=1.4f;
                        if (proximaAlavanca >= 3) { saveData.luaPuzzleConcluido=true; saveData.salvar(); gerarCaixasEInimigos(); caixasSpawnadas=true; mensagemAviso="PORTA DE SUPRIMENTOS LIBERADA!"; avisoTimer=2.5f; }
                    } else { proximaAlavanca=0; mensagemAviso="ORDEM ERRADA — RESET"; avisoTimer=2f; }
                    return;
                }
            }
        }
        if (saveData.cadaverAtivo && player.overlaps(new Rectangle(saveData.cadaverX, saveData.cadaverY, 48, 48)) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            RecoverySystems.recuperarCadaver(saveData); mensagemAviso="CADAVER RECUPERADO"; avisoTimer=2.5f; return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !saveData.segredoLuaEncontrado) {
            Vector3 cliqueMundo = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(cliqueMundo);
            if (SegredoSystem.checarClique(saveData, SegredoSystem.LUA, segredoLua, cliqueMundo.x, cliqueMundo.y)) {
                mensagemAviso = "Voce sentiu algo estranho sob o gelo...";
                avisoTimer = 2.5f;
                SoundManager.playSound("pickup");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            saveData.fase = "LUA"; saveData.playerX = player.x; saveData.playerY = player.y; saveData.salvar();
            mensagemAviso = "CHECKPOINT SALVO NA LUA!"; avisoTimer = 2.0f;
        }

        if (player.overlaps(npcLua) && !falouOficial && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            dialog.startChoice("Escolha: preservar a colonia ou exigir a rota de fuga?", portraitOficial);
            return;
        }

        for (int i = caixasCrafting.size - 1; i >= 0; i--) {
            ItemDrop caixa = caixasCrafting.get(i);
            if (player.overlaps(caixa.rect)) {
                caixasColetadas++; caixasCrafting.removeIndex(i);
                SoundManager.playSound("pickup");
                mensagemAviso = "PEÇA COLETADA (" + caixasColetadas + "/" + CAIXAS_NECESSARIAS + ")"; avisoTimer = 2.0f;
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && armaCraftada && !isReloading && saveData.municao < 25) {
            isReloading = true; reloadTimer = 1.5f; SoundManager.playSound("reload");
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
        WorldCollision.movePlayer(player, dx, dy, pedras, WORLD_WIDTH, WORLD_HEIGHT);
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

        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);
        camera.position.set(player.x, player.y, 0);

        if (player.overlaps(zonaInteracao)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !saveData.receitaCraftada) {
                mostrarCraft = true;
                if (RecoverySystems.craft(saveData.inventario, "GELO", "PECA", "FILTRO_O2")) {
                    saveData.receitaCraftada = true; saveData.questLog.complete("GELO"); saveData.salvar();
                    mensagemAviso = "CRAFT OK: FILTRO_O2!"; avisoTimer = 2.5f;
                } else { mensagemAviso = "FALTA MATERIAL: GELO + PECA"; avisoTimer = 2.5f; }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                if (caixasColetadas >= CAIXAS_NECESSARIAS) {
                    craftingProgress += delta;
                    if (craftingProgress >= 1.5f) {
                        if (!armaCraftada) { armaCraftada = true; saveData.inventario.temArma = true; saveData.temArma = true; saveData.inventario.municao = 25; saveData.municao = 25; saveData.luaMissoesOk = true; mensagemAviso = "SLASHWAVE FABRICADA! PORTAL DA CRATERA LIBERADO!"; saveData.salvar(); }
                        else { saveData.municao = 25; mensagemAviso = "MUNICÃO MAXIMA!"; }
                        craftingProgress = 0f; avisoTimer = 2.5f;
                    }
                } else {
                    mensagemAviso = "COLETE TODAS AS " + CAIXAS_NECESSARIAS + " PEÇAS PRIMEIRO!";
                    avisoTimer = 1.5f;
                }
            } else craftingProgress = 0f;
        } else craftingProgress = 0f;

        for (Enemy e : enemies) {
            if (e.ativo) {
                e.update(delta, new com.badlogic.gdx.math.Vector2(player.x, player.y));
                if (e.rect.overlaps(player)) receberDano(5f * delta);
            }
        }

        Enemy alvoDrone = RecoverySystems.nearestEnemy(enemies, player.x, player.y);
        if (drone.ativo) {
            boolean disparouDrone = drone.assist(delta, player.x, player.y, saveData,
                    alvoDrone != null ? alvoDrone.rect.x : player.x,
                    alvoDrone != null ? alvoDrone.rect.y : player.y,
                    alvoDrone != null);
            if (disparouDrone && alvoDrone != null) {
                float danoDrone = Math.max(4f, UpgradeSystem.danoArma(saveData) * 0.45f);
                alvoDrone.hp -= danoDrone;
                SoundManager.playSound("hit_enemy");
                if (alvoDrone.hp <= 0) {
                    alvoDrone.ativo = false;
                    UpgradeSystem.registerKill(saveData);
                }
            }
        }

        if (!armaCraftada && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && cooldown <= 0f) {
            socarGame();
        }

        if (armaCraftada && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && saveData.municao > 0 && !isReloading) {
            chargeTimer = Math.min(1.2f, chargeTimer + delta);
        } else if (armaCraftada && chargeTimer >= 0.8f && saveData.municao > 0 && !isReloading) {
            dispararGame(true); chargeTimer=0f;
        } else if (armaCraftada && chargeTimer > 0f && saveData.municao > 0 && !isReloading) {
            dispararGame(false); chargeTimer=0f;
        }
        if (armaCraftada && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && saveData.municao>0 && cooldown<=0f && !isReloading) dispararGame(false);

        for (int i = slashes.size - 1; i >= 0; i--) {
            SlashWave s = slashes.get(i); s.update(delta);
            if (!s.active) { slashes.removeIndex(i); continue; }
            for (Enemy e : enemies) {
                if (e.ativo && s.rect.overlaps(e.rect)) {
                    e.hp -= UpgradeSystem.danoArma(saveData) * s.damageMultiplier; s.active = false; SoundManager.playSound("hit_enemy");
                    if (somEnemyTimer <= 0) {
                        SoundManager.playSound("enemy");
                        somEnemyTimer = 25f;
                    }
                    if (e.hp <= 0) {
                        e.ativo = false;
                        if (UpgradeSystem.registerKill(saveData)) {
                            mensagemAviso = saveData.ultimoUpgrade;
                            avisoTimer = 2.5f;
                        }
                    }
                    break;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(portalParaMarte)) {
            if (armaCraftada && saveData.luaMissoesOk) {
                saveData.fase = "LUA_BOSS";
                saveData.salvar();
                fadingOut = true; nextScreen = new BossLuaScreen(game, saveData);
            } else {
                mensagemAviso = "COMPLETE AS MISSOES DA LUA E FABRIQUE A ARMA!"; avisoTimer = 2f;
            }
        }
    }


    private void socarGame() {
        Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(m);
        Rectangle hit = PlayerCombat.punchBox(player, m);
        estadoJogador = 2;
        slashAnimTimer = 0.28f;
        stateTime = 0f;
        cooldown = 0.28f;
        SoundManager.playSound("punch");
        for (Enemy e : enemies) {
            if (e.ativo && hit.overlaps(e.rect)) {
                e.hp -= 8f;
                SoundManager.playSound("hit_enemy");
                if (e.hp <= 0) {
                    e.ativo = false;
                    if (UpgradeSystem.registerKill(saveData)) { mensagemAviso = saveData.ultimoUpgrade; avisoTimer = 2.5f; }
                }
                break;
            }
        }
    }

    private void dispararGame(boolean charged) {
        estadoJogador=2; slashAnimTimer=.3f; stateTime=0f; saveData.municao--; saveData.inventario.municao=saveData.municao; cooldown=Math.max(.14f,.25f-.02f*saveData.inventario.nivelArma); SoundManager.playSound(charged ? "charged" : "slash"); Vector3 m=new Vector3(Gdx.input.getX(),Gdx.input.getY(),0); camera.unproject(m); slashes.add(new SlashWave(player.x,player.y,m.x,m.y,charged));
    }

    private void receberDano(float dano) {
        float resto = dano - saveData.escudo;
        saveData.escudo = Math.max(0f, saveData.escudo - dano);
        if (resto > 0f) UpgradeSystem.aplicarDano(saveData, resto);
        else SoundManager.playSound("hit_player");
    }

    @Override public void show() { Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None); SoundManager.playMusic("lua", true); } @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() { GameHud.reset(); } @Override public void dispose() { drone.dispose(); }
}
