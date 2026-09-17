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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.BossLua;
import com.modulo06.echoesmoon.entities.FoodDrop;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.entities.WorldRock;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.UpgradeSystem;
import com.modulo06.echoesmoon.systems.WorldCollision;

public class BossLuaScreen implements Screen {
    private final Game game;
    private final GameSaveData save;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shape = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final BossLua boss;
    private final Rectangle player = new Rectangle(100, 100, 32, 48);
    private final Array<SlashWave> pedrasBoss = new Array<>();
    private final Array<SlashWave> tirosJogador = new Array<>();
    private final Array<FoodDrop> comidas = new Array<>();
    private final Array<WorldRock> pedrasMapa = new Array<>();
    private final float WORLD_W = 800f, WORLD_H = 600f;
    private Texture fundoTex, bossTex, rockTex, foodTex, playerTex;
    private float cooldown = 0f;
    private float mensagemTimer = 0f;
    private String mensagem = "";

    public BossLuaScreen(Game game, GameSaveData save) {
        this.game = game;
        this.save = save;
        this.save.sincronizarInventario();
        camera.setToOrtho(false, WORLD_W, WORLD_H);
        boss = new BossLua(560, 400);
        carregarTexturas();
        spawnAmbient();
    }

    private void carregarTexturas() {
        fundoTex = load("fundo.png");
        bossTex = load("boss_lua.png");
        rockTex = load("rocha_boss.png");
        foodTex = load("food.png");
        playerTex = load("player_lunar.png");
    }

    private Texture load(String path) {
        return Gdx.files.internal(path).exists() ? new Texture(path) : null;
    }

    private void spawnAmbient() {
        Array<Rectangle> forbidden = new Array<>();
        forbidden.add(boss.rect);
        WorldRock.spawnMany(pedrasMapa, 10, WORLD_W, WORLD_H, player, forbidden, 150f);
        for (int i = 0; i < 4; i++) {
            float x = MathUtils.random(60f, WORLD_W - 60f);
            float y = MathUtils.random(60f, WORLD_H - 60f);
            Rectangle r = new Rectangle(x, y, 28, 28);
            if (!r.overlaps(player) && !r.overlaps(boss.rect)) comidas.add(new FoodDrop(x, y));
        }
    }

    @Override public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0.07f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.position.set(WORLD_W / 2f, WORLD_H / 2f, 0f);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (fundoTex != null) batch.draw(fundoTex, 0, 0, WORLD_W, WORLD_H);
        for (WorldRock rock : pedrasMapa) {
            if (rockTex != null) { batch.setColor(0.62f, 0.68f, 0.74f, 1f); batch.draw(rockTex, rock.rect.x, rock.rect.y, rock.rect.width, rock.rect.height); batch.setColor(Color.WHITE); }
        }
        for (FoodDrop food : comidas) if (foodTex != null) batch.draw(foodTex, food.rect.x, food.rect.y, food.rect.width, food.rect.height);
        if (boss.ativo && bossTex != null) batch.draw(bossTex, boss.rect.x, boss.rect.y, boss.rect.width, boss.rect.height);
        for (SlashWave shot : tirosJogador) if (shot.active && rockTex != null) batch.draw(rockTex, shot.rect.x, shot.rect.y, 16, 16, 32, 32, 0.75f, 0.75f, shot.angle, 0, 0, rockTex.getWidth(), rockTex.getHeight(), false, false);
        for (SlashWave rock : pedrasBoss) if (rock.active && rockTex != null) batch.draw(rockTex, rock.rect.x, rock.rect.y, 16, 16, 32, 32, 1.2f, 1.2f, rock.angle, 0, 0, rockTex.getWidth(), rockTex.getHeight(), false, false);
        if (playerTex != null) batch.draw(playerTex, player.x, player.y, player.width, player.height);
        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        if (boss.ativo && bossTex == null) boss.renderFallback(shape);
        shape.end();

        desenharHUD();
    }

    private void update(float delta) {
        if (!save.luaMissoesOk) { game.setScreen(new GameScreen(game, save)); return; }
        if (save.inventario.aberto) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) save.inventario.aberto = false;
            if (Gdx.input.isKeyJustPressed(Input.Keys.C)) save.inventario.usarComida(save);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) { save.inventario.aberto = true; return; }

        if (cooldown > 0f) cooldown -= delta;
        if (mensagemTimer > 0f) mensagemTimer -= delta;
        save.o2 -= 0.55f * delta;
        if (save.o2 <= 0f) { game.setScreen(new GameOverScreen(game)); return; }

        float dx = 0f, dy = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 220f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 220f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 220f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 220f * delta;
        WorldCollision.movePlayer(player, dx, dy, pedrasMapa, WORLD_W, WORLD_H);

        for (int i = comidas.size - 1; i >= 0; i--) {
            if (player.overlaps(comidas.get(i).rect)) { save.inventario.add("COMIDA"); comidas.removeIndex(i); mensagem = "+1 COMIDA"; mensagemTimer = 1.5f; }
        }

        boss.update(delta, new Vector2(player.x, player.y));
        if (boss.rect.overlaps(player)) UpgradeSystem.aplicarDano(save, boss.getContactDamage() * delta);
        if (boss.consumeRockThrow()) {
            pedrasBoss.add(new SlashWave(boss.rect.x + boss.rect.width / 2f, boss.rect.y + boss.rect.height / 2f, boss.getRockTarget().x, boss.getRockTarget().y));
        }

        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && cooldown <= 0f && save.inventario.municao > 0 && boss.ativo) {
            cooldown = Math.max(0.14f, 0.25f - 0.02f * save.inventario.nivelArma);
            save.inventario.municao--;
            save.municao = save.inventario.municao;
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            camera.unproject(mouse);
            SlashWave shot = new SlashWave(player.x, player.y, mouse.x, mouse.y);
            tirosJogador.add(shot);
        }

        for (int i = tirosJogador.size - 1; i >= 0; i--) {
            SlashWave shot = tirosJogador.get(i);
            shot.update(delta);
            if (boss.ativo && shot.rect.overlaps(boss.rect)) {
                boss.levarDano(UpgradeSystem.danoArma(save));
                shot.active = false;
                if (!boss.ativo) {
                    save.bossLuaDerrotado = true;
                    save.inventario.add("CHAVE_LUA");
                    save.inventario.municao = 25;
                    save.municao = 25;
                    save.fase = "MARTE";
                    save.salvar();
                    game.setScreen(new MarsScreen(game, save));
                    return;
                }
            }
            if (!shot.active) tirosJogador.removeIndex(i);
        }

        for (int i = pedrasBoss.size - 1; i >= 0; i--) {
            SlashWave p = pedrasBoss.get(i);
            p.update(delta);
            if (p.rect.overlaps(player)) { UpgradeSystem.aplicarDano(save, boss.getRockDamage()); p.active = false; }
            if (!p.active) pedrasBoss.removeIndex(i);
        }
    }

    private void desenharHUD() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "GUARDIAO DA CRATERA — LUA", 24, 575);
        font.draw(batch, "HP: " + boss.hp + " / " + boss.maxHp, 24, 548);
        font.draw(batch, "ARMA NV." + save.inventario.nivelArma + "  DANO " + UpgradeSystem.danoArma(save), 24, 520);
        font.draw(batch, "ARMADURA NV." + save.inventario.nivelArmadura, 24, 495);
        font.draw(batch, "O2/HP: " + (int)Math.max(0, save.o2), 24, 470);
        font.draw(batch, "I inventario | C comida | SPACE/CLICK ataca", 24, 28);
        if (mensagemTimer > 0) font.draw(batch, mensagem, 540, 30);
        batch.end();
        if (save.inventario.aberto) save.inventario.render(batch, font, camera.combined, save);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) { camera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); shape.dispose(); font.dispose(); if (fundoTex != null) fundoTex.dispose(); if (bossTex != null) bossTex.dispose(); if (rockTex != null) rockTex.dispose(); if (foodTex != null) foodTex.dispose(); }
}
