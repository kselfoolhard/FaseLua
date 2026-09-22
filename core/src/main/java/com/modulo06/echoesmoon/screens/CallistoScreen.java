package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.modulo06.echoesmoon.entities.BossCalisto;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.systems.CrosshairUtil;
import com.modulo06.echoesmoon.systems.DialogSystem;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SegredoSystem;
import com.modulo06.echoesmoon.systems.SoundManager;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;

/**
 * Chefe final, em Calisto. Segue o mesmo "sistema" visual das outras telas de
 * boss (camera acompanha o jogador, jogador animado, dialogo de intro, barra
 * de HP no topo, sem pedras no cenario, mira propria), mas mantem o que a
 * torna unica como ultimo chefe: 3 formas com dash, municao infinita durante
 * a luta e o portal para Aharin logo depois.
 */
public class CallistoScreen implements Screen {
    private final Game game;
    private final GameSaveData save;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final DialogSystem dialog = new DialogSystem();
    private final BossCalisto boss = new BossCalisto();
    private final Rectangle player = new Rectangle(100, 100, 32, 48);
    private final Rectangle portalAharin = new Rectangle(700, 250, 48, 80);
    private final Rectangle segredoCalisto = new Rectangle(40, 540, 40, 40);
    private final Array<FoodDrop> comidas = new Array<>();
    private final float WORLD_W = 800f, WORLD_H = 600f;
    private Texture fundoTex, bossTex, foodTex, iceTex;
    private Texture portraitBoss, alienTex;

    private Animation<TextureRegion> animIdle, animWalk, animSlash;
    private int estadoJogador = 0;
    private float slashAnimTimer = 0f;
    private float timerPasso = 0f;
    private float stateTime = 0f;

    private boolean inventarioAberto = false;
    private boolean mapaAberto = false;
    private float shootCooldown = 0f;
    private String mensagemHUD = "";
    private float mensagemTimer = 0f;

    public CallistoScreen(Game game, GameSaveData save) {
        this.game = game;
        this.save = save;
        save.sincronizarInventario();
        camera.setToOrtho(false, WORLD_W, WORLD_H);
        loadTextures();
        spawnAmbient();
        SoundManager.playMusic("boss", true);
        SoundManager.playSound("bossgrowl");

        dialog.start(new String[]{
            "Ate aqui voce chegou... impressionante, para um humano.",
            "Eu sou a ultima guardia de Calisto. Depois de mim, so resta a escolha final."
        }, portraitBoss != null ? portraitBoss : bossTex);
    }

    private void loadTextures() {
        fundoTex = load("fundo_calisto.png");
        bossTex = load("boss_calisto.png");
        foodTex = load("food.png");
        iceTex = load("ice.png");
        portraitBoss = load("portrait_boss.png");
        alienTex = load("alien.png");

        Texture idleTex = load("player_lunar.png");
        Texture walkTex = load("player_lunar_walk.png");
        Texture slashTexAnim = load("player_lunar_slash.png");
        if (idleTex != null) animIdle = new Animation<>(0.2f, TextureRegion.split(idleTex, idleTex.getWidth() / 4, idleTex.getHeight())[0]);
        if (walkTex != null) animWalk = new Animation<>(0.12f, TextureRegion.split(walkTex, walkTex.getWidth() / 4, walkTex.getHeight())[0]);
        if (slashTexAnim != null) animSlash = new Animation<>(0.1f, TextureRegion.split(slashTexAnim, slashTexAnim.getWidth() / 4, slashTexAnim.getHeight())[0]);
    }

    private Texture load(String p) { return Gdx.files.internal(p).exists() ? new Texture(p) : null; }

    private void spawnAmbient() {
        // As pedras do mapa foram removidas: a colisao delas estava ruim e atrapalhava
        // a movimentacao.
        Rectangle bossRect = new Rectangle(boss.x, boss.y, boss.width, boss.height);
        for (int i = 0; i < 6; i++) {
            float x = MathUtils.random(60f, WORLD_W - 60f), y = MathUtils.random(60f, WORLD_H - 60f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (!r.overlaps(player) && !r.overlaps(portalAharin) && !r.overlaps(bossRect)) comidas.add(new FoodDrop(x, y));
        }
    }

    @Override public void show() {}

    @Override public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0.0f, 0.08f, 0.22f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float halfW = WORLD_W / 2f, halfH = WORLD_H / 2f;
        float camX = MathUtils.clamp(player.x, halfW, WORLD_W - halfW);
        float camY = MathUtils.clamp(player.y, halfH, WORLD_H - halfH);
        camera.position.set(camX, camY, 0); camera.update();

        batch.setProjectionMatrix(camera.combined); batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, WORLD_W, WORLD_H);
        if (iceTex != null && !save.segredoCalistoEncontrado) {
            batch.setColor(1f, 1f, 1f, 0.22f);
            batch.draw(iceTex, segredoCalisto.x, segredoCalisto.y, segredoCalisto.width, segredoCalisto.height);
            batch.setColor(Color.WHITE);
        }
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        if (bossTex != null && !boss.mortoFinal) batch.draw(bossTex, boss.x, boss.y, boss.width, boss.height);

        TextureRegion frameAtual = null;
        if (estadoJogador == 2 && animSlash != null) frameAtual = animSlash.getKeyFrame(stateTime, false);
        else if (estadoJogador == 1 && animWalk != null) frameAtual = animWalk.getKeyFrame(stateTime, true);
        else if (animIdle != null) frameAtual = animIdle.getKeyFrame(stateTime, true);
        if (frameAtual != null) batch.draw(frameAtual, player.x, player.y, player.width, player.height);
        batch.end();

        shape.setProjectionMatrix(camera.combined); shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(save.inventario.chaveLuz ? Color.GOLD : Color.DARK_GRAY);
        shape.rect(portalAharin.x, portalAharin.y, portalAharin.width, portalAharin.height);
        if (!boss.mortoFinal && bossTex == null) boss.render(shape);
        shape.end();

        desenharHUD();

        CrosshairUtil.desenharMira(shape);
    }

    private void update(float delta) {
        if (dialog.isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) dialog.next();
            return;
        }

        if (inventarioAberto || mapaAberto) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) inventarioAberto = false;
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) mapaAberto = false;
            if (inventarioAberto && Gdx.input.isKeyJustPressed(Input.Keys.C)) save.inventario.usarComida(save);
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) { inventarioAberto = true; return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { mapaAberto = true; return; }

        shootCooldown -= delta;
        if (mensagemTimer > 0) mensagemTimer -= delta;
        save.o2 -= 0.65f * delta;
        if (save.o2 <= 0) { game.setScreen(new GameOverScreen(game)); return; }

        boolean moving = false;
        float dx = 0f, dy = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { dx -= 180f * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { dx += 180f * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { dy += 180f * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { dy -= 180f * delta; moving = true; }
        WorldCollision.movePlayer(player, dx, dy, new Array<>(), WORLD_W, WORLD_H);
        stateTime += delta;

        if (estadoJogador == 2) {
            slashAnimTimer -= delta;
            if (slashAnimTimer <= 0) estadoJogador = 0;
        } else if (moving) {
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

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !save.segredoCalistoEncontrado) {
            Vector3 cliqueMundo = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(cliqueMundo);
            if (SegredoSystem.checarClique(save, SegredoSystem.CALISTO, segredoCalisto, cliqueMundo.x, cliqueMundo.y)) {
                mensagemHUD = "Voce sentiu algo estranho sob o gelo..."; mensagemTimer = 2.5f;
                SoundManager.playSound("pickup");
            }
        }

        for (int i = comidas.size - 1; i >= 0; i--) if (player.overlaps(comidas.get(i).rect)) { save.inventario.add("COMIDA"); comidas.removeIndex(i); mensagemHUD = "+1 COMIDA!"; mensagemTimer = 1.5f; }

        boss.update(delta, player.x, player.y, save.inventario);
        if (!boss.mortoFinal) {
            float dxBoss = boss.x - player.x, dyBoss = boss.y - player.y;
            if (dxBoss * dxBoss + dyBoss * dyBoss < 70f * 70f) UpgradeSystem.aplicarDano(save, 12f * delta);
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
                && shootCooldown <= 0f
                && !boss.mortoFinal) {

            estadoJogador = 2;
            slashAnimTimer = 0.3f;
            stateTime = 0f;
            // Calisto: municao infinita durante a luta do chefe final.
            shootCooldown = Math.max(0.16f, 0.25f - 0.02f * save.inventario.nivelArma);
            SoundManager.playSound("slash");

            float dist = (float) Math.hypot(boss.x - player.x, boss.y - player.y);
            if (dist <= 330f) {
                boss.levarDano(UpgradeSystem.danoArma(save), save.inventario);
                SoundManager.playSound("hit_enemy");
            }
        }

        if (boss.mortoFinal && !save.bossCalistoDerrotado) {
            save.bossCalistoDerrotado = true;
            save.fase = "CALISTO";
            save.salvar();
            mensagemHUD = "CHAVE DE LUZ OBTIDA! PORTAL ONLINE.";
            mensagemTimer = 4f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(portalAharin)) {
            if (save.inventario.tem("CHAVE_LUZ")) {
                save.fase = "AHARIN";
                save.salvar();
                game.setScreen(new AharinScreen(game, save));
            } else {
                mensagemHUD = "BLOQUEADO — A chave de Luz ainda nao existe.";
                mensagemTimer = 2f;
            }
        }
    }

    private void desenharHUD() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        if (!boss.mortoFinal) {
            float barW = 260f;
            float barX = camera.position.x - barW / 2f;
            float barY = camera.position.y + WORLD_H / 2f - 34f;
            shape.setColor(0.2f, 0.2f, 0.2f, 1f);
            shape.rect(barX, barY, barW, 16);
            shape.setColor(0.8f, 0.1f, 0.5f, 1f);
            shape.rect(barX, barY, barW * Math.max(0, boss.hp / boss.hpMax), 16);
        }
        shape.end();

        batch.setProjectionMatrix(camera.combined); batch.begin();
        font.setColor(Color.WHITE);
        if (!boss.mortoFinal) {
            font.draw(batch, "GUARDIA DE CALISTO — FORMA " + boss.forma + "/3", camera.position.x - 120, camera.position.y + WORLD_H / 2f - 44);
        } else {
            font.draw(batch, "CHEFE DERROTADO!", camera.position.x - 70, camera.position.y + WORLD_H / 2f - 44);
        }
        batch.end();

        // HUD fixa e caixa de dialogo usam a mesma resolucao logica das outras
        // telas de boss (1280x720), independente do tamanho do mundo de Calisto.
        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        shape.setProjectionMatrix(batch.getProjectionMatrix());
        batch.begin();
        font.draw(batch, "ARMA NV." + save.inventario.nivelArma + " | DANO " + UpgradeSystem.danoArma(save), 24, 70);
        font.draw(batch, "ARMADURA NV." + save.inventario.nivelArmadura + " | O2/HP " + (int) Math.max(0, save.o2), 24, 45);
        font.draw(batch, "I inventario | M mapa | SPACE/CLICK ataca | E portal", 24, 22);
        if (mensagemTimer > 0) font.draw(batch, mensagemHUD, 360, 35);
        batch.end();

        if (inventarioAberto) save.inventario.render(batch, font, batch.getProjectionMatrix(), save);
        if (mapaAberto) {
            batch.begin();
            font.draw(batch, "=== MAPA ===", 500, 650);
            font.draw(batch, "LUA   MARTE   TITA   CALISTO [VOCE ESTA AQUI]", 420, 350);
            batch.end();
        }

        dialog.render(batch, shape, font);
    }

    @Override public void resize(int width, int height) { camera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { SoundManager.stopMusic(); }
    @Override public void dispose() { shape.dispose(); batch.dispose(); font.dispose(); if (fundoTex != null) fundoTex.dispose(); if (bossTex != null) bossTex.dispose(); if (foodTex != null) foodTex.dispose(); }
}
