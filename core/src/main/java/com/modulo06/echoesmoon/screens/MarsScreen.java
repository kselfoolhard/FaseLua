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

public class MarsScreen implements Screen {

    private Game game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // --- SPRITES (Coloque os arquivos na pasta 'assets') ---
    private Texture playerTex, enemyTex, crystalTex, bgTex;

    private final float WORLD_WIDTH = 2500f;
    private final float WORLD_HEIGHT = 2500f;

    private Rectangle player;
    private float playerSpeed = 340f; // Marte tem gravidade/movimento ligeiramente mais rapido
    private float o2 = 100f, o2DrainRate = 5f; // Atmosfera fina consome O2 mais rapido
    private boolean isDead = false;
    private boolean gameWon = false;

    // --- COMBATE EM MARTE ---
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private Rectangle attackBox;

    // --- OBJETIVOS DE MARTE ---
    private int crystalsCollected = 0;
    private final int TOTAL_CRYSTALS = 4;

    class MarsEnemy {
        Rectangle rect; float speed = 160f;
        public MarsEnemy(float x, float y) { this.rect = new Rectangle(x, y, 40, 40); }
    }

    class Crystal {
        Rectangle rect; boolean collected = false;
        public Crystal(float x, float y) { this.rect = new Rectangle(x, y, 30, 30); }
    }

    private Array<MarsEnemy> enemies;
    private Array<Crystal> crystals;

    public MarsScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        player = new Rectangle(100, 100, 32, 48);
        attackBox = new Rectangle(0, 0, 120, 120);

        // Tenta carregar as imagens da pasta assets (se nao achar, usa o fallback)
        playerTex = safeLoadTexture("player_marte.png");
        enemyTex = safeLoadTexture("alien.png");
        crystalTex = safeLoadTexture("crystal.png");
        bgTex = safeLoadTexture("fundo_marte.png");

        // Spawna inimigos e cristais em Marte
        enemies = new Array<>();
        enemies.add(new MarsEnemy(800, 800));
        enemies.add(new MarsEnemy(1200, 500));
        enemies.add(new MarsEnemy(1500, 1500));
        enemies.add(new MarsEnemy(400, 1800));

        crystals = new Array<>();
        crystals.add(new Crystal(900, 900));
        crystals.add(new Crystal(1800, 400));
        crystals.add(new Crystal(500, 1600));
        crystals.add(new Crystal(2200, 2200));
    }

    private Texture safeLoadTexture(String path) {
        try {
            if (Gdx.files.internal(path).exists()) return new Texture(path);
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void render(float delta) {
        update(delta);

        // Fundo vermelho marciano
        Gdx.gl.glClearColor(0.4f, 0.08f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // 1. DESENHO DE TEXTURAS / SPRITES DE FUNDO
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (bgTex != null) {
            batch.draw(bgTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }
        batch.end();

        // 2. DESENHO DE RETÂNGULOS (FALLBACK SE NÃO HOUVER SPRITE)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Cristais (se nao tiver sprite)
        if (crystalTex == null) {
            shapeRenderer.setColor(Color.CYAN);
            for (Crystal c : crystals) {
                if (!c.collected) shapeRenderer.rect(c.rect.x, c.rect.y, c.rect.width, c.rect.height);
            }
        }

        // Inimigos (se nao tiver sprite)
        if (enemyTex == null) {
            shapeRenderer.setColor(Color.PURPLE);
            for (MarsEnemy e : enemies) shapeRenderer.rect(e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        }

        // Player (se nao tiver sprite)
        if (playerTex == null) {
            shapeRenderer.setColor(isDead ? Color.GRAY : Color.GREEN);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        // Area de ataque visual
        if (isAttacking) {
            shapeRenderer.setColor(new Color(1, 0, 0, 0.4f));
            shapeRenderer.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
        }
        shapeRenderer.end();

        // 3. DESENHO DAS TEXTURAS/SPRITES DAS ENTIDADES
        batch.begin();
        if (crystalTex != null) {
            for (Crystal c : crystals) {
                if (!c.collected) batch.draw(crystalTex, c.rect.x, c.rect.y, c.rect.width, c.rect.height);
            }
        }
        if (enemyTex != null) {
            for (MarsEnemy e : enemies) batch.draw(enemyTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        }
        if (playerTex != null) {
            batch.draw(playerTex, player.x, player.y, player.width, player.height);
        }
        batch.end();

        // 4. HUD E TEXTOS
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        font.draw(batch, "FASE 2: MARTE | O2 Restante: " + (int)o2 + "%", 10, Gdx.graphics.getHeight() - 10);
        font.draw(batch, "Cristais de Terraformacao: " + crystalsCollected + " / " + TOTAL_CRYSTALS, 10, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "Controles: WASD/Seta = Move | ESPACO = Atacar Aliens", 10, Gdx.graphics.getHeight() - 50);

        if (gameWon) {
            font.getData().setScale(2f);
            font.draw(batch, "MISSÃO CUMPRIDA! MARTE FOI COLONIZADO!", 100, Gdx.graphics.getHeight() / 2);
        } else if (isDead) {
            font.getData().setScale(2f);
            font.draw(batch, "VOCE SUCUMBIU EM MARTE!", 200, Gdx.graphics.getHeight() / 2);
        }
        batch.end();
        font.getData().setScale(1f); // Reset escala da fonte
    }

    private void update(float delta) {
        if (isDead || gameWon) return;

        // Drenagem de O2
        o2 -= o2DrainRate * delta;
        if (o2 <= 0) { isDead = true; return; }

        // Movimento
        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= playerSpeed * delta;

        player.x += moveX; player.y += moveY;
        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);

        camera.position.set(
            MathUtils.clamp(player.x, camera.viewportWidth / 2f, WORLD_WIDTH - camera.viewportWidth / 2f),
            MathUtils.clamp(player.y, camera.viewportHeight / 2f, WORLD_HEIGHT - camera.viewportHeight / 2f), 0
        );

        // Coleta de Cristais
        for (Crystal c : crystals) {
            if (!c.collected && player.overlaps(c.rect)) {
                c.collected = true;
                crystalsCollected++;
                o2 = Math.min(100f, o2 + 25f); // Cada cristal recarrega um pouco do O2
                if (crystalsCollected >= TOTAL_CRYSTALS) {
                    gameWon = true;
                }
            }
        }

        // Inimigos Marcianos perseguem o jogador
        for (MarsEnemy e : enemies) {
            Vector2 dir = new Vector2(player.x - e.rect.x, player.y - e.rect.y).nor();
            e.rect.x += dir.x * e.speed * delta;
            e.rect.y += dir.y * e.speed * delta;

            if (player.overlaps(e.rect)) {
                o2 -= 15f * delta; // Alien suga O2 rapidamente
            }
        }

        // Ataque com a arma trazida da Lua
        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0) isAttacking = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            isAttacking = true;
            attackTimer = 0.2f;
            attackBox.set(player.x - 60, player.y - 60, player.width + 120, player.height + 120);

            for (int i = enemies.size - 1; i >= 0; i--) {
                if (attackBox.overlaps(enemies.get(i).rect)) {
                    enemies.removeIndex(i);
                }
            }
        }
    }

    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose(); shapeRenderer.dispose(); font.dispose();
        if (playerTex != null) playerTex.dispose();
        if (enemyTex != null) enemyTex.dispose();
        if (crystalTex != null) crystalTex.dispose();
        if (bgTex != null) bgTex.dispose();
    }
}
