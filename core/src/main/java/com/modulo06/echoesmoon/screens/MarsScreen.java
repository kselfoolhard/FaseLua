package com.modulo06.echoesmoon.screens;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.Bullet;
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;


public class MarsScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture fundoMarteTex, playerMarteTex, alienLunarTex, alienChaseTex, portalTex, iceTex, bulletTex;
    private Rectangle player, portalParaLua;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;
    private Array<Rectangle> gelo;

    private final float WORLD_WIDTH = 1200f;
    private final float WORLD_HEIGHT = 1200f;
    private float cooldown = 0f;
    private float saveIndicatorTimer = 0f;

    public MarsScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = saveData;

        camera = new OrthographicCamera(); camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        portalParaLua = new Rectangle(50, 50, 100, 100);
        player = new Rectangle(300, 300, 32, 48);
        bullets = new Array<>();

        carregarTexturas();
        gerarMundo();
    }

    private void carregarTexturas() {
        fundoMarteTex = safeLoad("fundo_marte.png");
        playerMarteTex = safeLoad("player_marte.png");
        alienLunarTex = safeLoad("alien_lunar.png");
        alienChaseTex = safeLoad("alien.png");
        portalTex = safeLoad("portal.png");
        iceTex = safeLoad("ice.png");
        bulletTex = safeLoad("bullet.png");
    }

    private Texture safeLoad(String path) {
        try { if (Gdx.files.internal(path).exists()) return new Texture(path); } catch (Exception ignored) {}
        return null;
    }

    private void gerarMundo() {
        enemies = new Array<>(); gelo = new Array<>();

        int enemyCount = 14;
        for (int i = 0; i < enemyCount; i++) {
            float ex = MathUtils.random(200, WORLD_WIDTH - 200);
            float ey = MathUtils.random(200, WORLD_HEIGHT - 200);
            int type = (i % 3 == 0) ? 1 : 0;
            enemies.add(new Enemy(ex, ey, type));
        }

        gelo.add(new Rectangle(600, 200, 32, 32));
        gelo.add(new Rectangle(900, 800, 32, 32));
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.4f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (fundoMarteTex != null) batch.draw(fundoMarteTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        if (portalTex != null) batch.draw(portalTex, portalParaLua.x, portalParaLua.y, portalParaLua.width, portalParaLua.height);

        for (Rectangle g : gelo) if (iceTex != null) batch.draw(iceTex, g.x, g.y, g.width, g.height);

        for (Enemy e : enemies) {
            if (e.ativo) {
                Texture texToDraw = (e.type == 0) ? alienLunarTex : alienChaseTex;
                if (texToDraw != null) batch.draw(texToDraw, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
            }
        }

        for (Bullet b : bullets) {
            if (b.active && bulletTex != null) {
                batch.draw(bulletTex, b.rect.x, b.rect.y, b.rect.width, b.rect.height);
            }
        }

        if (playerMarteTex != null) batch.draw(playerMarteTex, player.x, player.y, player.width, player.height);

        batch.end();

        // Healthbars dos inimigos e Crosshair do mouse em Marte
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Enemy e : enemies) {
            if (e.ativo) {
                float barWidth = 36f;
                float barHeight = 5f;
                float barX = e.rect.x;
                float barY = e.rect.y + e.rect.height + 6;

                shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);

                float healthRatio = (float) e.hp / e.maxHp;
                shapeRenderer.setColor(0.1f, 0.9f, 0.1f, 1);
                shapeRenderer.rect(barX, barY, barWidth * healthRatio, barHeight);
            }
        }

        // Crosshair em Marte
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        shapeRenderer.rect(mousePos.x - 8, mousePos.y - 1, 16, 2);
        shapeRenderer.rect(mousePos.x - 1, mousePos.y - 8, 2, 16);

        shapeRenderer.end();

        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        batch.begin();
        font.draw(batch, "MARTE - O2: " + (int)saveData.o2 + "% | MUNICAO: " + saveData.municao, 30, 690);
        font.draw(batch, "[CLIQUE ESQ] ATIRAR COM MIRA | ELIMINE OS INIMIGOS", 30, 660);

        if (saveIndicatorTimer > 0) {
            font.draw(batch, "[💾 JOGO SALVO COM SUCESSO!]", 1050, 690);
        }
        batch.end();
    }

    private void update(float delta) {
        if (cooldown > 0f) cooldown -= delta;
        if (saveIndicatorTimer > 0f) saveIndicatorTimer -= delta;

        saveData.o2 -= 2.5f * delta;
        saveData.energia -= 1.5f * delta;

        if (saveData.o2 <= 0 || saveData.energia <= 0) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            saveData.municao = 10;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.x -= 300 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.x += 300 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.y += 300 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.y -= 300 * delta;

        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);
        camera.position.set(player.x, player.y, 0);

        Vector2 pPos = new Vector2(player.x, player.y);
        for (Enemy e : enemies) {
            e.update(delta, pPos);
            if (e.ativo && player.overlaps(e.rect)) saveData.o2 -= 15f * delta;
        }

        // Sistema de Tiro com Mouse em Marte
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && saveData.temArma && saveData.municao > 0 && cooldown <= 0f) {
            saveData.municao--;
            cooldown = 0.2f;

            Vector3 mouseWorldPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseWorldPos);

            float startX = player.x + player.width / 2;
            float startY = player.y + player.height / 2;

            float dirX = mouseWorldPos.x - startX;
            float dirY = mouseWorldPos.y - startY;

            bullets.add(new Bullet(startX, startY, dirX, dirY));
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);
            if (!b.active) {
                bullets.removeIndex(i);
                continue;
            }
            for (Enemy e : enemies) {
                if (e.ativo && b.rect.overlaps(e.rect)) {
                    e.hp -= 25;
                    b.active = false;
                    if (e.hp <= 0) e.ativo = false;
                    break;
                }
            }
        }

        for (int i = gelo.size - 1; i >= 0; i--) {
            if (player.overlaps(gelo.get(i))) { saveData.o2 = Math.min(100f, saveData.o2 + 25f); gelo.removeIndex(i); }
        }

        if (player.overlaps(portalParaLua)) {
            saveData.missaoEtapa = 3;
            player.x += 150;
            saveData.playerX = player.x; saveData.playerY = player.y;
            saveData.fase = "LUA";
            saveData.salvar();
            game.setScreen(new GameScreen(game, saveData));
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); shapeRenderer.dispose(); font.dispose(); }
}
