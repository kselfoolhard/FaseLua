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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.SlashWave;
import com.modulo06.echoesmoon.systems.GameSaveData;

public class GameScreen implements Screen {
    private Game game;
    private GameSaveData saveData;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture fundoLuaTex, portalTex, slashTex, bancadaTex, alienTex, playerSheet;
    private Animation<TextureRegion> playerAnim;
    private float stateTime = 0f;

    private Rectangle player, portalParaMarte, bancada, zonaInteracao;
    private Array<SlashWave> slashes;
    private Array<Enemy> enemies;

    private final float WORLD_WIDTH = 1200f;
    private final float WORLD_HEIGHT = 1200f;

    private float cooldown = 0f, avisoTimer = 0f;
    private float craftingProgress = 0f; // Controle da barra
    private String mensagemAviso = "";
    private boolean mostrarQuest = true;

    // VARIÁVEL QUE TRAVA A ARMA
    private boolean armaCraftada = false;

    public GameScreen(Game game, GameSaveData saveData) {
        this.game = game; this.saveData = saveData;
        camera = new OrthographicCamera(); camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch(); shapeRenderer = new ShapeRenderer(); font = new BitmapFont();

        player = new Rectangle(300, 300, 32, 48);
        portalParaMarte = new Rectangle(1000, 1000, 100, 100);

        bancada = new Rectangle(400, 400, 64, 64);
        // Área um pouco maior que a bancada para permitir encostar e interagir
        zonaInteracao = new Rectangle(bancada.x - 20, bancada.y - 20, bancada.width + 40, bancada.height + 40);

        // Zera a munição no início para forçar o craft
        this.saveData.municao = 0;

        slashes = new Array<>(); enemies = new Array<>();
        for (int i = 0; i < 5; i++) enemies.add(new Enemy(MathUtils.random(500, 1100), MathUtils.random(500, 1100), 0));

        carregarTexturas();
    }

    private void carregarTexturas() {
        fundoLuaTex = safeLoad("fundo.png"); portalTex = safeLoad("portal.png");
        slashTex = safeLoad("slash_wave.png"); bancadaTex = safeLoad("bancada.png");
        alienTex = safeLoad("alien_lunar.png"); playerSheet = safeLoad("player_marte.png");
        if (playerSheet != null) {
            TextureRegion[][] tmp = TextureRegion.split(playerSheet, playerSheet.getWidth() / 4, playerSheet.getHeight());
            TextureRegion[] walkFrames = new TextureRegion[4]; int index = 0;
            for (int j = 0; j < 4; j++) walkFrames[index++] = tmp[0][j];
            playerAnim = new Animation<>(0.15f, walkFrames);
        }
    }

    private Texture safeLoad(String path) {
        try { if (Gdx.files.internal(path).exists()) return new Texture(path); } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // TEXTURAS
        batch.setProjectionMatrix(camera.combined); batch.begin();
        if (fundoLuaTex != null) batch.draw(fundoLuaTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        if (portalTex != null) batch.draw(portalTex, portalParaMarte.x, portalParaMarte.y, portalParaMarte.width, portalParaMarte.height);
        if (bancadaTex != null) batch.draw(bancadaTex, bancada.x, bancada.y, bancada.width, bancada.height);

        for (Enemy e : enemies) {
            if (e.ativo && alienTex != null) batch.draw(alienTex, e.rect.x, e.rect.y, e.rect.width, e.rect.height);
        }
        for (SlashWave s : slashes) {
            if (s.active && slashTex != null) batch.draw(slashTex, s.rect.x, s.rect.y, 32, 32, 64, 64, 1f, 1f, s.angle, 0, 0, slashTex.getWidth(), slashTex.getHeight(), false, false);
        }
        if (playerAnim != null) batch.draw(playerAnim.getKeyFrame(stateTime, true), player.x, player.y, player.width, player.height);
        else if (playerSheet != null) batch.draw(playerSheet, player.x, player.y, player.width, player.height);
        batch.end();

        // FORMAS (Barras de Vida e Barra de Crafting)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Enemy e : enemies) {
            if (e.ativo) {
                shapeRenderer.setColor(0.8f, 0f, 0f, 1); shapeRenderer.rect(e.rect.x, e.rect.y + e.rect.height + 5, e.rect.width, 5);
                shapeRenderer.setColor(0f, 0.8f, 0f, 1);
                float hpPercent = Math.max(0, e.hp / 100f);
                shapeRenderer.rect(e.rect.x, e.rect.y + e.rect.height + 5, e.rect.width * hpPercent, 5);
            }
        }

        // Desenhar Barra de Crafting (Segurando E)
        if (craftingProgress > 0) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(bancada.x, bancada.y + bancada.height + 15, bancada.width, 8);
            shapeRenderer.setColor(0.9f, 0.7f, 0.1f, 1f); // Dourado
            shapeRenderer.rect(bancada.x, bancada.y + bancada.height + 15, bancada.width * (craftingProgress / 1.5f), 8);
        }
        shapeRenderer.end();

        desenharHUDL4D();
    }

    private void desenharHUDL4D() {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, 1280, 720);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (mostrarQuest) {
            shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.6f); shapeRenderer.rect(20, 580, 350, 120);
            shapeRenderer.setColor(1f, 1f, 1f, 0.8f); shapeRenderer.rectLine(20, 580, 20, 700, 4);
        }

        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.6f); shapeRenderer.rect(20, 20, 250, 80);
        shapeRenderer.setColor(0.1f, 0.8f, 0.2f, 1f); shapeRenderer.rect(30, 30, 230 * (saveData.o2 / 100f), 15);
        shapeRenderer.end(); Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "+ " + (int)saveData.o2, 30, 75);

        font.getData().setScale(1.5f);
        // Só mostra a munição se a arma foi craftada
        font.draw(batch, armaCraftada ? String.valueOf(saveData.municao) : "DESARMADO", 1120, 350);
        font.getData().setScale(1f);

        if (mostrarQuest) {
            font.draw(batch, "OBJETIVOS ATUAIS", 40, 680);
            font.draw(batch, armaCraftada ? "- Elimine as ameacas locais." : "- Va a bancada e fabrique a arma.", 40, 650);
            font.draw(batch, "- Encontre o portal para Marte.", 40, 630);
        }
        font.draw(batch, "[TAB] Ocultar Objetivos | [F5] Salvar", 20, 715);
        if (avisoTimer > 0) font.draw(batch, mensagemAviso, 600, 100);

        batch.setProjectionMatrix(camera.combined);

        // Texto dinâmico em cima da bancada!
        if (player.overlaps(zonaInteracao) && craftingProgress == 0) {
            String texto = armaCraftada ? "[E] CRAFTAR MUNICÃO" : "SEGURE [E] PARA ARMA";
            font.draw(batch, texto, bancada.x - 30, bancada.y - 10);
        }

        if (player.overlaps(portalParaMarte)) font.draw(batch, "[E] IR PARA MARTE", portalParaMarte.x, portalParaMarte.y - 20);
        batch.end();
    }

    private void update(float delta) {
        if (cooldown > 0f) cooldown -= delta;
        if (avisoTimer > 0f) avisoTimer -= delta;

        saveData.o2 -= 0.5f * delta;
        if (saveData.o2 <= 0) game.setScreen(new GameOverScreen(game));

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) mostrarQuest = !mostrarQuest;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) { saveData.salvar(); mensagemAviso = "JOGO SALVO!"; avisoTimer = 2.0f; }

        float oldX = player.x;
        float oldY = player.y;

        boolean moving = false;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { player.x -= 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) { player.x += 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { player.y += 300 * delta; moving = true; }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) { player.y -= 300 * delta; moving = true; }
        if (moving) stateTime += delta;

        // Colisão Sólida perfeita (Impede o jogador de entrar na bancada, mas não joga ele pra longe)
        if (player.overlaps(bancada)) {
            player.x = oldX;
            player.y = oldY;
        }

        player.x = MathUtils.clamp(player.x, 0, WORLD_WIDTH - player.width);
        player.y = MathUtils.clamp(player.y, 0, WORLD_HEIGHT - player.height);
        camera.position.set(player.x, player.y, 0);

        // Sistema de Crafting (Usa a Zona de Interação, não a caixa sólida)
        if (player.overlaps(zonaInteracao)) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                craftingProgress += delta;
                if (craftingProgress >= 1.5f) { // 1.5 Segundos pra encher a barra
                    if (!armaCraftada) {
                        armaCraftada = true;
                        saveData.municao = 25;
                        mensagemAviso = "SLASHWAVE FABRICADA!";
                    } else {
                        saveData.municao = Math.min(25, saveData.municao + 15);
                        mensagemAviso = "MUNICÃO RECARREGADA!";
                    }
                    craftingProgress = 0f;
                    avisoTimer = 2f;
                }
            } else {
                craftingProgress = 0f; // Reseta se soltar o E antes da hora
            }
        } else {
            craftingProgress = 0f;
        }

        for (Enemy e : enemies) {
            if (e.ativo) {
                Vector2 dir = new Vector2(player.x - e.rect.x, player.y - e.rect.y);
                if (dir.len() > 0) { e.rect.x += dir.nor().x * 40 * delta; e.rect.y += dir.nor().y * 40 * delta; }
                if (e.rect.overlaps(player)) saveData.o2 -= 5f * delta;
            }
        }

        // Tiro TRAVADO enquanto não craftar a arma!
        if (armaCraftada && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && saveData.municao > 0 && cooldown <= 0f) {
            saveData.municao--; cooldown = 0.25f;
            Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(m); slashes.add(new SlashWave(player.x, player.y, m.x, m.y));
        }

        for (int i = slashes.size - 1; i >= 0; i--) {
            SlashWave s = slashes.get(i); s.update(delta);
            if (!s.active) { slashes.removeIndex(i); continue; }
            for (Enemy e : enemies) {
                if (e.ativo && s.rect.overlaps(e.rect)) {
                    e.hp -= 40; s.active = false;
                    if (e.hp <= 0) e.ativo = false;
                    break;
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && player.overlaps(portalParaMarte)) {
            saveData.fase = "MARTE"; saveData.salvar(); game.setScreen(new MarsScreen(game, saveData));
        }
    }
    @Override public void show() {} @Override public void resize(int w, int h) {}
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {} @Override public void dispose() {}
}
