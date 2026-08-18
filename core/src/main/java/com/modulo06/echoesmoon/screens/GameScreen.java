package com.modulo06.echoesmoon.screens;

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
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.EchoesMoonGame;

public class GameScreen implements Screen {

    private EchoesMoonGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private final float WORLD_WIDTH = 2000f;
    private final float WORLD_HEIGHT = 2000f;

    private Rectangle player;
    private float playerSpeed = 300f;

    // Timer de sobrevivência (30 segundos para vitória)
    private float gameTimer = 30.0f;

    // Variáveis de Textura (Sprites)
    private Texture bgTex;
    private Texture playerTex;
    private Texture baseTex;
    private Texture o2Tex;
    private Texture foodTex;
    private Texture iceTex;
    private Texture obstacleTex;

    // Status do Jogador
    private float o2 = 100f;
    private float maxO2 = 100f;
    private float o2ConsumptionRate = 5f;

    private float energy = 100f;
    private float maxEnergy = 100f;
    private float energyConsumptionRate = 3f; // Drena energia com o tempo

    private int inventoryIce = 0;
    private int inventoryWater = 0;
    private int inventoryFuel = 0;
    private boolean isDead = false;

    private Rectangle base;
    private boolean isNearBase = false;

    // Sistema de Obstáculos Fixos (Pedras Grandes)
    class Obstacle {
        Rectangle rect;
        public Obstacle(float x, float y, float width, float height) {
            this.rect = new Rectangle(x, y, width, height);
        }
    }
    private Array<Obstacle> obstacles;

    // Sistema de Partículas de Rastro
    class Particle {
        float x, y, alpha;
        public Particle(float x, float y) {
            this.x = x;
            this.y = y;
            this.alpha = 1.0f;
        }
    }
    private Array<Particle> particles = new Array<>();

    // Sistema de Itens
    enum ItemType { O2, FOOD, ICE }
    class Item {
        Rectangle rect;
        ItemType type;
        boolean collected = false;
        Color color;

        public Item(float x, float y, ItemType type) {
            this.rect = new Rectangle(x, y, 32, 32);
            this.type = type;
            if (type == ItemType.O2) color = Color.CYAN;
            else if (type == ItemType.FOOD) color = Color.GREEN;
            else color = Color.WHITE;
        }
    }
    private Array<Item> items;

    public GameScreen(EchoesMoonGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        player = new Rectangle(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 32, 48);
        base = new Rectangle(WORLD_WIDTH / 2 - 200, WORLD_HEIGHT / 2 - 100, 150, 100);

        // Inicializando Obstáculos Fixos (Pedras Grandes) pelo mapa
        obstacles = new Array<>();
        obstacles.add(new Obstacle(WORLD_WIDTH / 2 + 150, WORLD_HEIGHT / 2 + 150, 100, 80));
        obstacles.add(new Obstacle(WORLD_WIDTH / 2 - 250, WORLD_HEIGHT / 2 + 200, 120, 90));
        obstacles.add(new Obstacle(WORLD_WIDTH / 2 - 300, WORLD_HEIGHT / 2 - 250, 90, 110));
        obstacles.add(new Obstacle(WORLD_WIDTH / 2 + 200, WORLD_HEIGHT / 2 - 200, 140, 70));

        // Inicializando Itens espalhados
        items = new Array<>();
        items.add(new Item(WORLD_WIDTH / 2 + 100, WORLD_HEIGHT / 2 + 300, ItemType.O2));
        items.add(new Item(WORLD_WIDTH / 2 - 350, WORLD_HEIGHT / 2 + 50, ItemType.O2));
        items.add(new Item(WORLD_WIDTH / 2 + 300, WORLD_HEIGHT / 2 - 150, ItemType.FOOD));
        items.add(new Item(WORLD_WIDTH / 2 - 100, WORLD_HEIGHT / 2 - 350, ItemType.FOOD));
        items.add(new Item(WORLD_WIDTH / 2 + 400, WORLD_HEIGHT / 2 + 100, ItemType.ICE));
        items.add(new Item(WORLD_WIDTH / 2 - 400, WORLD_HEIGHT / 2 - 200, ItemType.ICE));


        game.assets.load("fundo.png", Texture.class);
        game.assets.load("player.png", Texture.class);
        game.assets.load("base.png", Texture.class);
        game.assets.load("o2.png", Texture.class);
        game.assets.load("food.png", Texture.class);
        game.assets.load("ice.png", Texture.class);
        game.assets.load("pedra.png", Texture.class); // NOVO: Carrega o sprite da pedra
        game.assets.finishLoading();

        bgTex = game.assets.get("fundo.png", Texture.class);
        playerTex = game.assets.get("player.png", Texture.class);
        baseTex = game.assets.get("base.png", Texture.class);
        o2Tex = game.assets.get("o2.png", Texture.class);
        foodTex = game.assets.get("food.png", Texture.class);
        iceTex = game.assets.get("ice.png", Texture.class);
        obstacleTex = game.assets.get("pedra.png", Texture.class); // NOVO: Atribui a textura da pedra
    }

    private Texture getTextureForItem(ItemType type) {
        switch(type) {
            case O2: return o2Tex;
            case FOOD: return foodTex;
            case ICE: return iceTex;
        }
        return null;
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // 1. Fundo
        if (bgTex != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.draw(bgTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.end();
        }

        // 2. Formas Geométricas (Fallback e Obstáculos)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Desenha Obstáculos (Pedras Grandes - Cinza Escuro)
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (Obstacle obs : obstacles) {
            shapeRenderer.rect(obs.rect.x, obs.rect.y, obs.rect.width, obs.rect.height);
        }

        // Desenha Base se não tiver sprite
        if (baseTex == null) {
            shapeRenderer.setColor(Color.NAVY);
            shapeRenderer.rect(base.x, base.y, base.width, base.height);
        }

        // Desenha Itens sem sprite
        for (Item item : items) {
            if (!item.collected && getTextureForItem(item.type) == null) {
                shapeRenderer.setColor(item.color);
                shapeRenderer.rect(item.rect.x, item.rect.y, item.rect.width, item.rect.height);
            }
        }

        // Desenha Partículas de rastro
        for (Particle p : particles) {
            p.alpha -= delta * 2;
            shapeRenderer.setColor(0.6f, 0.6f, 0.6f, Math.max(0, p.alpha));
            shapeRenderer.circle(p.x, p.y, 4);
        }

        // Desenha Player sem sprite
        if (playerTex == null) {
            if (isDead) shapeRenderer.setColor(Color.GRAY);
            else shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }
        shapeRenderer.end();

        // Limpa partículas invisíveis
        for (int i = particles.size - 1; i >= 0; i--) {
            if (particles.get(i).alpha <= 0) particles.removeIndex(i);
        }

        // 3. Sprites e Textos Dinâmicos no Mundo
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (baseTex != null) batch.draw(baseTex, base.x, base.y, base.width, base.height);

        for (Item item : items) {
            if (!item.collected) {
                Texture tex = getTextureForItem(item.type);
                if (tex != null) batch.draw(tex, item.rect.x, item.rect.y, item.rect.width, item.rect.height);
            }
        }

        if (playerTex != null) batch.draw(playerTex, player.x, player.y, player.width, player.height);

        // Apenas aviso de interação na base (o título "BASE LUNAR" foi removido)
        if (isNearBase && !isDead) {
            font.draw(batch, "[Recarregando] Aperte 'E' p/ processar Gelo", base.x, base.y - 15);
        }

        if (isDead) {
            font.setColor(Color.RED);
            font.draw(batch, "MISSAO FALHOU - SEM OXIGENIO!", player.x - 100, player.y + 50);
            font.setColor(Color.WHITE);
        }
        batch.end();

        // 4. HUD Fixo na Tela
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        if (!isDead) {
            font.draw(batch, "O2: " + (int)o2 + "%", 10, Gdx.graphics.getHeight() - 10);
            font.draw(batch, "Energia: " + (int)energy + "%", 10, Gdx.graphics.getHeight() - 30);
            font.draw(batch, "Tempo Restante: " + (int)gameTimer + "s", 10, Gdx.graphics.getHeight() - 50);
            font.draw(batch, "Gelo (Inv): " + inventoryIce, 10, Gdx.graphics.getHeight() - 70);
            font.draw(batch, "Agua: " + inventoryWater + " | Combustivel: " + inventoryFuel, 10, Gdx.graphics.getHeight() - 90);
        }
        batch.end();
    }

    private void update(float delta) {
        if (isDead) return;

        // Timer de Sobrevivência (90 segundos para Vitória)
        gameTimer -= delta;
        if (gameTimer <= 0) {
            game.setScreen(new EndGameScreen(game, "VITORIA! Voce sobreviveu na Lua!"));
            return;
        }

        // Movimentação eixo X com colisão nas pedras
        float moveX = 0;
        float moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += playerSpeed * delta;

        player.x += moveX;
        if (checkObstacleCollision()) {
            player.x -= moveX; // reverte se bateu
        }

        // Movimentação eixo Y com colisão nas pedras
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += playerSpeed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= playerSpeed * delta;

        player.y += moveY;
        if (checkObstacleCollision()) {
            player.y -= moveY; // reverte se bateu
        }

        // Se moveu, solta partícula de rastro
        if ((moveX != 0 || moveY != 0)) {
            particles.add(new Particle(player.x + 16, player.y));
        }

        // Limites do mundo
        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);

        // Câmera seguindo o player
        float camX = MathUtils.clamp(player.x, camera.viewportWidth / 2f, WORLD_WIDTH - camera.viewportWidth / 2f);
        float camY = MathUtils.clamp(player.y, camera.viewportHeight / 2f, WORLD_HEIGHT - camera.viewportHeight / 2f);
        camera.position.set(camX, camY, 0);

        // Interação com a Base
        isNearBase = player.overlaps(base);
        if (isNearBase) {
            o2 += (o2ConsumptionRate * 3) * delta;
            if (o2 > maxO2) o2 = maxO2;

            energy += (energyConsumptionRate * 3) * delta;
            if (energy > maxEnergy) energy = maxEnergy;

            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && inventoryIce > 0) {
                inventoryIce--;
                inventoryWater++;
                inventoryFuel++;
                o2 = maxO2;
                energy = maxEnergy;
            }
        } else {
            // Consumo contínuo fora da base
            o2 -= o2ConsumptionRate * delta;
            energy -= energyConsumptionRate * delta;

            if (o2 <= 0 || energy <= 0) {
                o2 = 0;
                energy = 0;
                isDead = true;
                game.setScreen(new EndGameScreen(game, "GAME OVER! Recursos esgotados."));
            }
        }

        // Coleta de Itens
        for (Item item : items) {
            if (!item.collected && player.overlaps(item.rect)) {
                item.collected = true;
                switch (item.type) {
                    case O2:
                        o2 += 30;
                        if (o2 > maxO2) o2 = maxO2;
                        break;
                    case FOOD:
                        energy += 35;
                        if (energy > maxEnergy) energy = maxEnergy;
                        break;
                    case ICE:
                        inventoryIce++;
                        break;
                }
            }
        }
    }

    private boolean checkObstacleCollision() {
        for (Obstacle obs : obstacles) {
            if (player.overlaps(obs.rect)) {
                return true;
            }
        }
        return false;
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
