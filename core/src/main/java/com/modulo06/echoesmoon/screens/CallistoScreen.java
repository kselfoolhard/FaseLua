package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.BossCalisto;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.WorldRock;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;

public class CallistoScreen implements Screen {
    private final Game game;
    private final GameSaveData save;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final BossCalisto boss = new BossCalisto();
    private final Rectangle player = new Rectangle(100, 100, 32, 48);
    private final Rectangle portalAharin = new Rectangle(700, 250, 48, 80);
    private final Array<WorldRock> pedras = new Array<>();
    private final Array<FoodDrop> comidas = new Array<>();
    private final float WORLD_W = 800f, WORLD_H = 600f;
    private Texture fundoTex, bossTex, rockTex, foodTex, playerTex;
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
        hudCamera.setToOrtho(false, WORLD_W, WORLD_H);
        loadTextures();
        spawnAmbient();
    }

    private void loadTextures() {
        fundoTex = load("fundo_calisto.png");
        bossTex = load("boss_calisto.png");
        rockTex = load("pedra.png");
        foodTex = load("food.png");
        playerTex = load("player_lunar.png");
    }

    private Texture load(String p) { return Gdx.files.internal(p).exists() ? new Texture(p) : null; }

    private void spawnAmbient() {
        Array<Rectangle> forbidden = new Array<>();
        forbidden.add(player);
        forbidden.add(portalAharin);
        Rectangle bossRect = new Rectangle(boss.x, boss.y, boss.width, boss.height);
        forbidden.add(bossRect);
        WorldRock.spawnMany(pedras, 14, WORLD_W, WORLD_H, player, forbidden, 160f);
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
        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0); camera.update();

        batch.setProjectionMatrix(camera.combined); batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, WORLD_W, WORLD_H);
        for (WorldRock rock : pedras) if (rockTex != null) { batch.setColor(0.55f, 0.70f, 0.82f, 1f); batch.draw(rockTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height); batch.setColor(Color.WHITE); }
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        if (bossTex != null && !boss.mortoFinal) batch.draw(bossTex, boss.x, boss.y, boss.width, boss.height);
        if (playerTex != null) batch.draw(playerTex, player.x, player.y, player.width, player.height);
        batch.end();

        shape.setProjectionMatrix(camera.combined); shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(save.inventario.chaveLuz ? Color.GOLD : Color.DARK_GRAY);
        shape.rect(portalAharin.x, portalAharin.y, portalAharin.width, portalAharin.height);
        if (!boss.mortoFinal && bossTex == null) boss.render(shape);
        shape.end();

        desenharHUD();
    }

    private void update(float delta) {
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

        float dx = 0f, dy = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 180f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 180f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 180f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 180f * delta;
        WorldCollision.movePlayer(player, dx, dy, pedras, WORLD_W, WORLD_H);

        for (int i = comidas.size - 1; i >= 0; i--) if (player.overlaps(comidas.get(i).rect)) { save.inventario.add("COMIDA"); comidas.removeIndex(i); mensagemHUD = "+1 COMIDA!"; mensagemTimer = 1.5f; }

        boss.update(delta, player.x, player.y, save.inventario);
        if (!boss.mortoFinal) {
            float dxBoss = boss.x - player.x, dyBoss = boss.y - player.y;
            if (dxBoss * dxBoss + dyBoss * dyBoss < 70f * 70f) UpgradeSystem.aplicarDano(save, 12f * delta);
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
                && shootCooldown <= 0f
                && !boss.mortoFinal) {

            // Calisto: munição infinita durante a luta do boss.
            shootCooldown = Math.max(0.16f, 0.25f - 0.02f * save.inventario.nivelArma);

            float dist = (float)Math.hypot(boss.x - player.x, boss.y - player.y);
            if (dist <= 330f) {
                boss.levarDano(UpgradeSystem.danoArma(save), save.inventario);
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
        batch.setProjectionMatrix(hudCamera.combined); batch.begin();
        font.setColor(Color.WHITE);
        if (!boss.mortoFinal) {
            font.draw(batch, "BOSS CALISTO FORMA " + boss.forma + " / 3", 280, 570);
            font.draw(batch, "HP: " + (int)boss.hp + " / " + (int)boss.hpMax, 330, 545);
        } else {
            font.draw(batch, "CHEFE DERROTADO!", 300, 570);
        }
        font.draw(batch, "ARMA NV." + save.inventario.nivelArma + " | DANO " + UpgradeSystem.danoArma(save), 24, 70);
        font.draw(batch, "ARMADURA NV." + save.inventario.nivelArmadura + " | O2/HP " + (int)Math.max(0, save.o2), 24, 45);
        font.draw(batch, "I inventario | M mapa | SPACE/CLICK ataca | E portal", 24, 22);
        if (mensagemTimer > 0) font.draw(batch, mensagemHUD, 360, 35);
        batch.end();

        if (inventarioAberto) save.inventario.render(batch, font, hudCamera.combined, save);
        if (mapaAberto) {
            batch.setProjectionMatrix(hudCamera.combined); batch.begin();
            font.draw(batch, "=== MAPA ===", 340, 500);
            font.draw(batch, "LUA   MARTE   TITA   CALISTO [VOCE ESTA AQUI]", 110, 150);
            batch.end();
        }
    }

    @Override public void resize(int width, int height) { camera.update(); hudCamera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { shape.dispose(); batch.dispose(); font.dispose(); if (fundoTex != null) fundoTex.dispose(); if (bossTex != null) bossTex.dispose(); if (rockTex != null) rockTex.dispose(); if (foodTex != null) foodTex.dispose(); }
}
