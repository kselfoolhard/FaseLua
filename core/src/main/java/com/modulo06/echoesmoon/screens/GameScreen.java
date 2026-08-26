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

public class GameScreen implements Screen {

    private Game game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // --- SPRITES (Coloque os arquivos na pasta 'assets') ---
    private Texture playerTex, enemyTex, baseTex, portalTex, itemTex, bgTex;

    private final float WORLD_WIDTH = 2000f;
    private final float WORLD_HEIGHT = 2000f;

    private Rectangle player;
    private float playerSpeed = 300f;
    private float gameTimer = 300.0f;

    // Status
    private float o2 = 100f, maxO2 = 100f, o2ConsumptionRate = 3f;
    private float energy = 100f, maxEnergy = 100f, energyConsumptionRate = 2f;
    private boolean isDead = false;

    // --- INVENTÁRIO DE MISSÃO ---
    private boolean hasPartAntenna = false;
    private boolean hasPartEnergy = false;
    private boolean hasPartExtractor = false;
    private boolean hasPartGreenhouse = false;
    private boolean hasWeaponA = false, hasWeaponB = false, hasWeaponC = false;
    private boolean hasWeapon = false;

    // --- ESTADOS DA BASE ---
    private Rectangle base;
    private boolean isNearBase = false;
    private boolean repAntenna = false, repEnergy = false, repExtractor = false, repGreenhouse = false;

    // --- PORTAL MARTE ---
    private Rectangle portal;

    // --- COMBATE ---
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private Rectangle attackBox;

    class Obstacle {
        Rectangle rect;
        public Obstacle(float x, float y, float w, float h) { this.rect = new Rectangle(x, y, w, h); }
    }

    class Enemy {
        Rectangle rect; float hp = 100; float speed = 120f;
        public Enemy(float x, float y) { this.rect = new Rectangle(x, y, 32, 32); }
    }

    class RepairStation {
        Rectangle rect; String name; Color color;
        public RepairStation(float x, float y, String name, Color color) {
            this.rect = new Rectangle(x, y, 40, 40); this.name = name; this.color = color;
        }
    }

    enum ItemType { O2, FOOD, ICE, PART_ANTENNA, PART_ENERGY, PART_EXTRACTOR, PART_GREENHOUSE, WEP_A, WEP_B, WEP_C }
    class Item {
        Rectangle rect; ItemType type; boolean collected = false; Color color;
        public Item(float x, float y, ItemType type) {
            this.rect = new Rectangle(x, y, 32, 32); this.type = type;
            if (type == ItemType.O2) color = Color.CYAN;
            else if (type == ItemType.FOOD) color = Color.GREEN;
            else if (type.toString().startsWith("PART")) color = Color.YELLOW;
            else if (type.toString().startsWith("WEP")) color = Color.MAGENTA;
            else color = Color.WHITE;
        }
    }

    private Array<Obstacle> obstacles;
    private Array<Item> items;
    private Array<Enemy> enemies;
    private Array<RepairStation> stations;

    public GameScreen(Game game) {
        this.game = game;
        camera = new OrthographicCamera(); camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch(); shapeRenderer = new ShapeRenderer(); font = new BitmapFont();

        player = new Rectangle(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 32, 48);
        base = new Rectangle(WORLD_WIDTH / 2 - 200, WORLD_HEIGHT / 2 - 100, 200, 150);
        portal = new Rectangle(WORLD_WIDTH - 150, WORLD_HEIGHT - 150, 100, 100);
        attackBox = new Rectangle(0, 0, 100, 100);

        // Carregamento dos Sprites
        playerTex = safeLoadTexture("player_lunar.png");
        enemyTex = safeLoadTexture("alien_lunar.png");
        baseTex = safeLoadTexture("base.png");
        portalTex = safeLoadTexture("portal.png");
        itemTex = safeLoadTexture("item.png");
        bgTex = safeLoadTexture("fundo.png");

        obstacles = new Array<>();
        obstacles.add(new Obstacle(WORLD_WIDTH / 2 + 150, WORLD_HEIGHT / 2 + 150, 100, 80));

        stations = new Array<>();
        stations.add(new RepairStation(base.x - 50, base.y + 50, "Antena", Color.LIGHT_GRAY));
        stations.add(new RepairStation(base.x + 220, base.y + 50, "Gerador", Color.YELLOW));
        stations.add(new RepairStation(base.x + 80, base.y + 170, "Extrator", Color.BLUE));
        stations.add(new RepairStation(base.x + 80, base.y - 50, "Estufa", Color.LIME));

        enemies = new Array<>();
        enemies.add(new Enemy(WORLD_WIDTH / 2 + 500, WORLD_HEIGHT / 2 + 300));
        enemies.add(new Enemy(WORLD_WIDTH / 2 - 500, WORLD_HEIGHT / 2 - 400));
        enemies.add(new Enemy(WORLD_WIDTH / 2 + 300, WORLD_HEIGHT / 2 - 600));

        items = new Array<>();
        items.add(new Item(WORLD_WIDTH / 2 + 100, WORLD_HEIGHT / 2 + 300, ItemType.PART_ANTENNA));
        items.add(new Item(WORLD_WIDTH / 2 - 350, WORLD_HEIGHT / 2 + 50, ItemType.PART_ENERGY));
        items.add(new Item(WORLD_WIDTH / 2 + 300, WORLD_HEIGHT / 2 - 150, ItemType.PART_EXTRACTOR));
        items.add(new Item(WORLD_WIDTH / 2 - 100, WORLD_HEIGHT / 2 - 350, ItemType.PART_GREENHOUSE));

        items.add(new Item(WORLD_WIDTH / 2 + 600, WORLD_HEIGHT / 2 + 600, ItemType.WEP_A));
        items.add(new Item(WORLD_WIDTH / 2 - 600, WORLD_HEIGHT / 2 + 600, ItemType.WEP_B));
        items.add(new Item(WORLD_WIDTH / 2 - 600, WORLD_HEIGHT / 2 - 600, ItemType.WEP_C));

        items.add(new Item(WORLD_WIDTH / 2 + 200, WORLD_HEIGHT / 2 + 200, ItemType.O2));
        items.add(new Item(WORLD_WIDTH / 2 - 200, WORLD_HEIGHT / 2 - 200, ItemType.FOOD));
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

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // 1. FUNDO
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (bgTex != null) {
            batch.draw(bgTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }
        batch.end();

        // 2. RETÂNGULOS (FALLBACK SE NÃO HOUVER SPRITES)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (portalTex == null) {
            shapeRenderer.setColor(Color.PURPLE);
            shapeRenderer.rect(portal.x, portal.y, portal.width, portal.height);
        }

        shapeRenderer.setColor(Color.DARK_GRAY);
        for (Obstacle obs : obstacles) shapeRenderer.rect(obs.rect.x, obs.rect.y, obs.rect.width, obs.rect.height);

        if (baseTex == null) {
            shapeRenderer.setColor(Color.NAVY);
            shapeRenderer.rect(base.x, base.y, base.width, base.height);
        }

        for(RepairStation s : stations) {
            shapeRenderer.setColor(s.color);
            shapeRenderer.rect(s.rect.x, s.rect.y, s.rect.width, s.rect.height);
        }

        if (itemTex == null) {
            for (Item item : items) {
                if (!item.collected) {
                    shapeRenderer.setColor(item.color);
                    shapeRenderer.rect(item.rect.x, item.rect.y, item.rect.width, item.rect.height);
                }
            }
        }

        if (enemyTex == null) {
            shapeRenderer.setColor(Color.RED);
            for (Enemy e : enemies) shapeRenderer.rect(e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        }

        if (playerTex == null) {
            shapeRenderer.setColor(isDead ? Color.GRAY : Color.ORANGE);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }

        if (isAttacking) {
            shapeRenderer.setColor(new Color(1, 1, 0, 0.5f));
            shapeRenderer.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
        }

        shapeRenderer.end();

        // 3. DESENHO DOS SPRITES DAS ENTIDADES
        batch.begin();
        if (portalTex != null) batch.draw(portalTex, portal.x, portal.y, portal.width, portal.height);
        if (baseTex != null) batch.draw(baseTex, base.x, base.y, base.width, base.height);

        if (itemTex != null) {
            for (Item item : items) {
                if (!item.collected) batch.draw(itemTex, item.rect.x, item.rect.y, item.rect.width, item.rect.height);
            }
        }

        if (enemyTex != null) {
            for (Enemy e : enemies) batch.draw(enemyTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        }

        if (playerTex != null) batch.draw(playerTex, player.x, player.y, player.width, player.height);
        batch.end();

        // 4. TEXTOS E HUD
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.draw(batch, "PORTAL MARTE", portal.x, portal.y - 10);
        font.draw(batch, "BASE LUNAR (Tecle E p/ Craftar Arma)", base.x, base.y - 10);

        for(RepairStation s : stations) {
            boolean isRep = false;
            if(s.name.equals("Antena")) isRep = repAntenna;
            if(s.name.equals("Gerador")) isRep = repEnergy;
            if(s.name.equals("Extrator")) isRep = repExtractor;
            if(s.name.equals("Estufa")) isRep = repGreenhouse;

            String txt = s.name + (isRep ? " [ON]" : " [OFF - Use 'E']");
            font.draw(batch, txt, s.rect.x - 20, s.rect.y + 60);
        }

        if (isDead) font.draw(batch, "GAME OVER", player.x, player.y + 50);
        batch.end();

        // HUD FIXO NA TELA
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        font.draw(batch, "FASE 1: LUA | O2: " + (int)o2 + "% | Energia: " + (int)energy + "% | Tempo: " + (int)gameTimer + "s", 10, Gdx.graphics.getHeight() - 10);

        String inv = "Pecas: ";
        if(hasPartAntenna) inv += "[Antena] ";
        if(hasPartEnergy) inv += "[Gerador] ";
        if(hasPartExtractor) inv += "[Extrator] ";
        if(hasPartGreenhouse) inv += "[Estufa] ";
        font.draw(batch, inv, 10, Gdx.graphics.getHeight() - 30);

        String armaTxt = "Partes da Arma: " + (hasWeaponA?"A ":"") + (hasWeaponB?"B ":"") + (hasWeaponC?"C ":"");
        if(hasWeapon) armaTxt = "ARMA EQUIPADA! (Espaco para Atacar)";
        font.draw(batch, armaTxt, 10, Gdx.graphics.getHeight() - 50);

        String obj = "Objetivo: ";
        if(!hasWeapon && !repAntenna) obj += "Encontre pecas e conserte a base.";
        else if (hasWeaponA && hasWeaponB && hasWeaponC && !hasWeapon) obj += "Va para a Base craftar a arma!";
        else if (repAntenna && repEnergy && repExtractor && repGreenhouse) obj += "Sistemas Online! Portal Liberado!";
        else obj += "Sobreviva e conserte sistemas.";
        font.draw(batch, obj, 10, Gdx.graphics.getHeight() - 70);

        batch.end();
    }

    private void update(float delta) {
        if (isDead) return;
        gameTimer -= delta;
        if (gameTimer <= 0) { isDead = true; return; }

        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= playerSpeed * delta;

        player.x += moveX; player.y += moveY;
        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);

        camera.position.set(MathUtils.clamp(player.x, camera.viewportWidth / 2f, WORLD_WIDTH - camera.viewportWidth / 2f),
            MathUtils.clamp(player.y, camera.viewportHeight / 2f, WORLD_HEIGHT - camera.viewportHeight / 2f), 0);

        isNearBase = player.overlaps(base);
        if (isNearBase) {
            o2 += 15f * delta; if (o2 > maxO2) o2 = maxO2;
            energy += 15f * delta; if (energy > maxEnergy) energy = maxEnergy;

            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && hasWeaponA && hasWeaponB && hasWeaponC && !hasWeapon) {
                hasWeapon = true;
            }
        } else {
            o2 -= o2ConsumptionRate * delta; energy -= energyConsumptionRate * delta;
            if (o2 <= 0 || energy <= 0) isDead = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            for(RepairStation s : stations) {
                if(player.overlaps(s.rect)) {
                    if(s.name.equals("Antena") && hasPartAntenna) { repAntenna = true; s.color = Color.GREEN; }
                    if(s.name.equals("Gerador") && hasPartEnergy) { repEnergy = true; s.color = Color.GREEN; }
                    if(s.name.equals("Extrator") && hasPartExtractor) { repExtractor = true; s.color = Color.GREEN; }
                    if(s.name.equals("Estufa") && hasPartGreenhouse) { repGreenhouse = true; s.color = Color.GREEN; }
                }
            }
        }

        for (Item item : items) {
            if (!item.collected && player.overlaps(item.rect)) {
                item.collected = true;
                switch (item.type) {
                    case O2: o2 += 30; break; case FOOD: energy += 30; break;
                    case PART_ANTENNA: hasPartAntenna = true; break;
                    case PART_ENERGY: hasPartEnergy = true; break;
                    case PART_EXTRACTOR: hasPartExtractor = true; break;
                    case PART_GREENHOUSE: hasPartGreenhouse = true; break;
                    case WEP_A: hasWeaponA = true; break;
                    case WEP_B: hasWeaponB = true; break;
                    case WEP_C: hasWeaponC = true; break;
                }
            }
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            Vector2 dir = new Vector2(player.x - e.rect.x, player.y - e.rect.y).nor();
            e.rect.x += dir.x * e.speed * delta;
            e.rect.y += dir.y * e.speed * delta;

            if (player.overlaps(e.rect)) {
                o2 -= 20 * delta; energy -= 20 * delta;
            }
        }

        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0) isAttacking = false;
        }

        if (hasWeapon && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            isAttacking = true;
            attackTimer = 0.2f;
            attackBox.set(player.x - 50, player.y - 50, player.width + 100, player.height + 100);

            for (int i = enemies.size - 1; i >= 0; i--) {
                if (attackBox.overlaps(enemies.get(i).rect)) {
                    enemies.removeIndex(i);
                }
            }
        }

        if (player.overlaps(portal)) {
            if (hasWeapon || (repAntenna && repEnergy && repExtractor && repGreenhouse)) {
                game.setScreen(new MarsScreen(game));
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
        if (baseTex != null) baseTex.dispose();
        if (portalTex != null) portalTex.dispose();
        if (itemTex != null) itemTex.dispose();
        if (bgTex != null) bgTex.dispose();
    }
}
