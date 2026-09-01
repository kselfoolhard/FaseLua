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
import com.modulo06.echoesmoon.systems.MissionState;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class GameScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture playerTex, baseTex, portalTex, fundoTex, pedraTex, foodTex, o2Tex, itemTex, alienLunarTex, alienChaseTex, bulletTex;

    private final float WORLD_WIDTH = 1200f;
    private final float WORLD_HEIGHT = 1200f;

    private Rectangle player, base, portal, caixaItem;
    private Array<Rectangle> pedras, comidas, tanquesO2;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;

    private MissionState mission;
    private float cooldown = 0f;
    private float saveIndicatorTimer = 0f;

    private float repairProgress = 0f;
    private final float REPAIR_TIME = 1.5f; // Reduzido para 1.5 segundos
    private float hordeTimer = 0f;

    public GameScreen(Game game, GameSaveData saveData) {
        this.game = game;
        this.saveData = (saveData != null) ? saveData : new GameSaveData();

        camera = new OrthographicCamera(); camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        mission = new MissionState();
        mission.setEtapaIndex(this.saveData.missaoEtapa);

        player = new Rectangle(this.saveData.playerX, this.saveData.playerY, 32, 48);
        base = new Rectangle(WORLD_WIDTH / 2 - 100, WORLD_HEIGHT / 2 - 100, 200, 150);
        portal = new Rectangle(WORLD_WIDTH - 150, WORLD_HEIGHT - 150, 100, 100);
        caixaItem = new Rectangle(150, 150, 40, 40);

        bullets = new Array<>();
        carregarTexturas();
        gerarMundo();
    }

    private void carregarTexturas() {
        playerTex = safeLoad("player_lunar.png");
        alienLunarTex = safeLoad("alien_lunar.png");
        alienChaseTex = safeLoad("alien.png");
        baseTex = safeLoad("base.png");
        portalTex = safeLoad("portal.png");
        fundoTex = safeLoad("fundo.png");
        pedraTex = safeLoad("pedra.png");
        foodTex = safeLoad("food.png");
        o2Tex = safeLoad("o2.png");
        itemTex = safeLoad("item.png");
        bulletTex = safeLoad("bullet.png");
    }

    private Texture safeLoad(String path) {
        try { if (Gdx.files.internal(path).exists()) return new Texture(path); } catch (Exception ignored) {}
        return null;
    }

    private void gerarMundo() {
        pedras = new Array<>(); comidas = new Array<>(); tanquesO2 = new Array<>(); enemies = new Array<>();

        pedras.add(new Rectangle(300, 400, 80, 80)); pedras.add(new Rectangle(800, 200, 80, 80));
        comidas.add(new Rectangle(200, 800, 32, 32)); tanquesO2.add(new Rectangle(400, 200, 32, 32));

        int enemyCount = (mission.getEtapaIndex() >= 3) ? 12 : 7;
        for (int i = 0; i < enemyCount; i++) {
            float ex = MathUtils.random(200, WORLD_WIDTH - 200);
            float ey = MathUtils.random(200, WORLD_HEIGHT - 200);
            int type = (i % 2 == 0) ? 0 : 1;
            enemies.add(new Enemy(ex, ey, type));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (fundoTex != null) batch.draw(fundoTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        if (baseTex != null) batch.draw(baseTex, base.x, base.y, base.width, base.height);
        if (portalTex != null) batch.draw(portalTex, portal.x, portal.y, portal.width, portal.height);

        for (Rectangle p : pedras) if (pedraTex != null) batch.draw(pedraTex, p.x, p.y, p.width, p.height);
        for (Rectangle c : comidas) if (foodTex != null) batch.draw(foodTex, c.x, c.y, c.width, c.height);
        for (Rectangle t : tanquesO2) if (o2Tex != null) batch.draw(o2Tex, t.x, t.y, t.width, t.height);

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

        if (!saveData.pecaEstufa && itemTex != null) batch.draw(itemTex, caixaItem.x, caixaItem.y, caixaItem.width, caixaItem.height);
        if (playerTex != null) batch.draw(playerTex, player.x, player.y, player.width, player.height);

        batch.end();

        // Renderização de ShapeRenderer (Healthbars, barra de progresso e Crosshair do mouse)
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

        if (!saveData.repEstufa && saveData.pecaEstufa && player.overlaps(base)) {
            float pWidth = 140f;
            float pHeight = 12f;
            float pX = base.x + (base.width - pWidth) / 2;
            float pY = base.y + base.height + 25;

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
            shapeRenderer.rect(pX, pY, pWidth, pHeight);

            shapeRenderer.setColor(0.2f, 0.8f, 1.0f, 1);
            float progressRatio = repairProgress / REPAIR_TIME;
            shapeRenderer.rect(pX, pY, pWidth * progressRatio, pHeight);
        }

        // Desenhar Mira (Crosshair) na posição exata do Mouse no mundo
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        shapeRenderer.rect(mousePos.x - 8, mousePos.y - 1, 16, 2);
        shapeRenderer.rect(mousePos.x - 1, mousePos.y - 8, 2, 16);

        shapeRenderer.end();

        // HUD
        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        batch.begin();
        font.draw(batch, "O2: " + (int)saveData.o2 + "% | ENERGIA: " + (int)saveData.energia + "%", 30, 690);
        font.draw(batch, "MISSAO: " + mission.getAtual(), 30, 660);
        font.draw(batch, "MUNICAO: " + saveData.municao + " | [CLIQUE ESQ] ATIRAR COM MIRA | Segure [E] na Base", 30, 630);

        if (!saveData.repEstufa && saveData.pecaEstufa && player.overlaps(base)) {
            font.draw(batch, ">>> MANTENHA [E] PRESSIONADO PARA CONSERTAR A ESTUFA <<<", 400, 350);
        }

        if (saveIndicatorTimer > 0) {
            font.draw(batch, "[💾 JOGO SALVO COM SUCESSO!]", 1050, 690);
        }
        batch.end();
    }

    private void update(float delta) {
        if (cooldown > 0f) cooldown -= delta;
        if (saveIndicatorTimer > 0f) saveIndicatorTimer -= delta;

        saveData.o2 -= 2f * delta;
        saveData.energia -= 1.5f * delta;

        if (saveData.o2 <= 0 || saveData.energia <= 0) {
            game.setScreen(new GameOverScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            saveData.municao = 10;
        }

        if (mission.getEtapaIndex() < 2) {
            hordeTimer += delta;
            if (hordeTimer >= 22f) {
                hordeTimer = 0f;
                enemies.add(new Enemy(player.x + MathUtils.random(-200, 200), player.y + MathUtils.random(-200, 200), 1));
                enemies.add(new Enemy(player.x + MathUtils.random(-200, 200), player.y + MathUtils.random(-200, 200), 0));
            }
        }

        float oldX = player.x; float oldY = player.y;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.x -= 300 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.x += 300 * delta;
        for (Rectangle p : pedras) if (player.overlaps(p)) player.x = oldX;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.y += 300 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.y -= 300 * delta;
        for (Rectangle p : pedras) if (player.overlaps(p)) player.y = oldY;

        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);
        camera.position.set(player.x, player.y, 0);

        for (int i = comidas.size - 1; i >= 0; i--) {
            if (player.overlaps(comidas.get(i))) { saveData.energia = Math.min(100f, saveData.energia + 30f); comidas.removeIndex(i); }
        }
        for (int i = tanquesO2.size - 1; i >= 0; i--) {
            if (player.overlaps(tanquesO2.get(i))) { saveData.o2 = Math.min(100f, saveData.o2 + 30f); tanquesO2.removeIndex(i); }
        }

        if (!saveData.pecaEstufa && player.overlaps(caixaItem)) {
            saveData.pecaEstufa = true;
            mission.avancarPara(1);
            salvarProgresso();
        }

        // Reparo rápido com 'E'
        if (!saveData.repEstufa && saveData.pecaEstufa && player.overlaps(base)) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                repairProgress += delta;
                if (repairProgress >= REPAIR_TIME) {
                    saveData.repEstufa = true;
                    saveData.temArma = true;
                    mission.avancarPara(2);
                    salvarProgresso();
                }
            }
        } else {
            repairProgress = 0f;
        }

        Vector2 pPos = new Vector2(player.x, player.y);
        for (Enemy e : enemies) {
            e.update(delta, pPos);
            if (e.ativo && player.overlaps(e.rect)) saveData.o2 -= 12f * delta;
        }

        // Sistema de Tiro Controlado via Mouse (Botão Esquerdo)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && saveData.temArma && saveData.municao > 0 && cooldown <= 0f) {
            saveData.municao--;
            cooldown = 0.2f;

            // Pega a posição do mouse no mundo
            Vector3 mouseWorldPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouseWorldPos);

            float startX = player.x + player.width / 2;
            float startY = player.y + player.height / 2;

            // Direção exata do player até o cursor do mouse
            float dirX = mouseWorldPos.x - startX;
            float dirY = mouseWorldPos.y - startY;

            bullets.add(new Bullet(startX, startY, dirX, dirY));
        }

        // Atualizar Projéteis e Colisões precisas
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);
            if (!b.active) {
                bullets.removeIndex(i);
                continue;
            }
            for (Enemy e : enemies) {
                if (e.ativo && b.rect.overlaps(e.rect)) {
                    e.hp -= 25; // Dano por tiro
                    b.active = false;
                    if (e.hp <= 0) e.ativo = false;
                    break;
                }
            }
        }

        if (mission.getEtapaIndex() >= 3) {
            boolean todosMortos = true;
            for (Enemy e : enemies) {
                if (e.ativo) { todosMortos = false; break; }
            }
            if (todosMortos) {
                game.setScreen(new VictoryScreen(game));
                return;
            }
        }

        if (player.overlaps(portal)) {
            if (saveData.temArma) mission.avancarPara(3);
            player.x -= 150;
            saveData.playerX = player.x; saveData.playerY = player.y; saveData.missaoEtapa = mission.getEtapaIndex();
            saveData.fase = "MARTE";
            salvarProgresso();
            game.setScreen(new MarsScreen(game, saveData));
        }
    }

    private void salvarProgresso() {
        saveData.salvar();
        saveIndicatorTimer = 2.0f;
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); shapeRenderer.dispose(); font.dispose(); }
}
