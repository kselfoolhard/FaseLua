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

public class GameScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture fundoLuaTex, portalTex, slashTex, bancadaTex, alienTex, portraitOficial, npcTex, baseTex, caixaTex, foodTex, rockTex;

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;
    private float somEnemyTimer = 0f;

    private Rectangle player, portalParaMarte, bancada, zonaInteracao, npcLua, safeZone;
    private Array<SlashWave> slashes;
    private Array<Enemy> enemies;
    private Array<ItemDrop> caixasCrafting;
    private Array<FoodDrop> comidas;
    private Array<WorldRock> pedras;
    private DialogSystem dialog;

    private final float WORLD_WIDTH = 1200f;
    private final float WORLD_HEIGHT = 1200f;

    private float cooldown = 0f, avisoTimer = 0f, reloadTimer = 0f;
    private float craftingProgress = 0f;
    private String mensagemAviso = "";
    private boolean mostrarQuest = true;
    private boolean falouOficial = false;
    private boolean caixasSpawnadas = false;
    private boolean armaCraftada = false;
    private boolean isReloading = false;

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

        player = new Rectangle(300, 300, 32, 48);
        portalParaMarte = new Rectangle(1000, 1000, 100, 100);
        bancada = new Rectangle(500, 400, 64, 64);
        zonaInteracao = new Rectangle(bancada.x - 20, bancada.y - 20, bancada.width + 40, bancada.height + 40);

        npcLua = new Rectangle(250, 350, 36, 54);
        safeZone = new Rectangle(180, 280, 160, 160);

        this.saveData.sincronizarInventario();
        this.armaCraftada = this.saveData.inventario.temArma;
        this.falouOficial = this.saveData.luaMissoesOk;
        slashes = new Array<>();
        enemies = new Array<>();
        caixasCrafting = new Array<>();
        comidas = new Array<>();
        pedras = new Array<>();

        carregarTexturas();
        spawnAmbientObjects();
        SoundManager.playMusic("lua", true);
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
        rockTex = safeLoad("pedra.png");

        Texture idleTex = safeLoad("player_lunar.png");
        Texture walkTex = safeLoad("player_lunar_walk.png");
        Texture slashTexAnim = safeLoad("player_lunar_slash.png");

        if (idleTex != null) animIdle = new Animation<>(0.2f, TextureRegion.split(idleTex, idleTex.getWidth() / 4, idleTex.getHeight())[0]);
        if (walkTex != null) animWalk = new Animation<>(0.12f, TextureRegion.split(walkTex, walkTex.getWidth() / 4, walkTex.getHeight())[0]);
        if (slashTexAnim != null) animSlash = new Animation<>(0.1f, TextureRegion.split(slashTexAnim, slashTexAnim.getWidth() / 4, slashTexAnim.getHeight())[0]);
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
        WorldRock.spawnMany(pedras, 28, WORLD_WIDTH, WORLD_HEIGHT, player, forbidden, 180f);
        for (int i = 0; i < 10; i++) {
            float x = MathUtils.random(80f, WORLD_WIDTH - 80f);
            float y = MathUtils.random(80f, WORLD_HEIGHT - 80f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (r.overlaps(player) || r.overlaps(safeZone) || r.overlaps(bancada) || r.overlaps(npcLua) || r.overlaps(portalParaMarte)) continue;
            comidas.add(new FoodDrop(x, y));
        }
    }

    private void gerarCaixasEInimigos() {
        caixasCrafting.add(new ItemDrop(700, 200, 1));
        caixasCrafting.add(new ItemDrop(900, 600, 1));
        caixasCrafting.add(new ItemDrop(300, 900, 1));

        for (int i = 0; i < 5; i++) {
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

        for (WorldRock rock : pedras) {
            if (rockTex != null) {
                batch.setColor(0.62f, 0.68f, 0.74f, 1f);
                batch.draw(rockTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        for (ItemDrop caixa : caixasCrafting) if (caixaTex != null) batch.draw(caixaTex, caixa.rect.x, caixa.rect.y, caixa.rect.width, caixa.rect.height);
        for (Enemy e : enemies) if (e.ativo && alienTex != null) batch.draw(alienTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        for (SlashWave s : slashes) if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 24, 24, 48, 48, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);

        TextureRegion frameAtual = null;
        if (estadoJogador == 2 && animSlash != null) frameAtual = animSlash.getKeyFrame(stateTime, false);
        else if (estadoJogador == 1 && animWalk != null) frameAtual = animWalk.getKeyFrame(stateTime, true);
        else if (animIdle != null) frameAtual = animIdle.getKeyFrame(stateTime, true);

        if (frameAtual != null) batch.draw(frameAtual, player.x, player.y, player.width, player.height);

        if (player.overlaps(npcLua) && !dialog.isOpen()) font.draw(batch, "[E] FALAR COM OFICIAL", npcLua.x - 30, npcLua.y + 57);
        if (player.overlaps(zonaInteracao) && craftingProgress == 0 && !dialog.isOpen()) {
            if (!armaCraftada) font.draw(batch, caixasColetadas >= CAIXAS_NECESSARIAS ? "SEGURE [E] PARA CRAFTAR SLASHWAVE" : "COLETE " + caixasColetadas + "/" + CAIXAS_NECESSARIAS + " PEÇAS!", bancada.x - 40, bancada.y - 10);
            else font.draw(batch, "[E] RECARREGAR MUNIÇÃO", bancada.x - 20, bancada.y - 10);
        }

        if (player.overlaps(portalParaMarte)) font.draw(batch, armaCraftada ? "[E] IR PARA A CRATERA" : "PORTAL BLOQUEADO (REQUER SLASHWAVE)", portalParaMarte.x - 20, portalParaMarte.y - 20);

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
            saveData.inventario.render(batch, font, batch.getProjectionMatrix(), saveData);
        }
    }

    private void desenharHUD() {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (mostrarQuest) {
            shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.6f);
            shapeRenderer.rect(20, 540, 370, 160);
            shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
            shapeRenderer.rectLine(20, 540, 20, 700, 4);
        }

        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.8f);
        shapeRenderer.rect(20, 20, 250, 100);
        shapeRenderer.setColor(0.1f, 0.5f, 0.8f, 1f);
        shapeRenderer.rect(30, 30, 230 * (Math.max(0, saveData.o2) / 100f), 15);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "O2: " + (int)Math.max(0, saveData.o2), 30, 75);
        font.draw(batch, "MUNICÃO: " + (armaCraftada ? saveData.municao : "N/A"), 30, 95);
        font.draw(batch, "PLANETA: LUA", 30, 115);

        if (isReloading) font.draw(batch, "RECARREGANDO...", 130, 95);

        if (mostrarQuest) {
            font.draw(batch, "OBJETIVOS ATUAIS", 40, 680);
            if (!falouOficial) font.draw(batch, "- Fale com o Oficial na base.", 40, 650);
            else if (caixasColetadas < CAIXAS_NECESSARIAS) font.draw(batch, "- Colete peças pelo mapa (" + caixasColetadas + "/" + CAIXAS_NECESSARIAS + ")", 40, 650);
            else if (!armaCraftada) font.draw(batch, "- Vá para a bancada e fabrique a arma.", 40, 650);
            else font.draw(batch, "- Entre no portal da cratera para enfrentar o Guardiao da Lua.", 40, 650);
        }

        font.draw(batch, "[TAB] Ocultar Quest | [I] Inventario | [F5] Salvar Checkpoint | [R] Recarregar", 20, 715);
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

        if (player.overlaps(safeZone)) saveData.o2 = Math.min(100f, saveData.o2 + 15f * delta);
        else saveData.o2 -= 0.5f * delta;

        if (saveData.o2 <= 0) game.setScreen(new GameOverScreen(game));

        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                dialog.next();
                if (!dialog.isOpen() && player.overlaps(npcLua)) {
                    falouOficial = true;
                    if (!caixasSpawnadas) { gerarCaixasEInimigos(); caixasSpawnadas = true; }
                }
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) mostrarQuest = !mostrarQuest;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            saveData.fase = "LUA"; saveData.salvar();
            mensagemAviso = "CHECKPOINT SALVO NA LUA!"; avisoTimer = 2.0f;
        }

        if (player.overlaps(npcLua) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            dialog.start(new String[]{
                "Recruta! Os sistemas da base falharam e a area foi invadida.",
                "Encontre 3 caixas de mantimentos espalhadas pelo setor.",
                "Traga-as ate a bancada para montar sua arma SlashWave",
                "Assim poderá ir para marte!"
            }, portraitOficial);
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
                if (e.rect.overlaps(player)) UpgradeSystem.aplicarDano(saveData, 5f * delta);
            }
        }

        if (armaCraftada && (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && saveData.municao > 0 && cooldown <= 0f && !isReloading) {
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

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {} @Override public void dispose() {}
}
