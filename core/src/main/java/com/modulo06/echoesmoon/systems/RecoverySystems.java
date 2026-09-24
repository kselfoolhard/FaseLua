package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.Enemy;
import java.util.LinkedHashSet;
import java.util.Set;

/** Sistemas compactos da recuperação: mantêm estado e lógica fora das telas. */
public final class RecoverySystems {
    private RecoverySystems() {}

    public static final class Quest {
        public final String id, titulo;
        public boolean feita;
        public Quest(String id, String titulo) { this.id = id; this.titulo = titulo; }
    }

    public static final class QuestLog {
        public final Array<Quest> quests = new Array<>();
        public QuestLog() {
            quests.add(new Quest("GELO", "Recuperar pecas e materiais na Lua"));
            quests.add(new Quest("ESTUFA", "Fabricar e melhorar a SlashWave"));
            quests.add(new Quest("BOSS", "Derrotar os chefes e alcancar Calisto"));
        }
        public void complete(String id) { for (Quest q : quests) if (q.id.equals(id)) q.feita = true; }
        public boolean isComplete(String id) { for (Quest q : quests) if (q.id.equals(id)) return q.feita; return false; }
    }

    public static final class Drone {
        public final Vector2 position = new Vector2();
        public final Vector2 lastShotTarget = new Vector2();
        public boolean ativo;
        private float shotCooldown = 0f;
        private float muzzleTimer = 0f;
        private Texture sprite;

        public void update(float delta, float playerX, float playerY, boolean olhandoEsquerda) {
            update(delta, playerX, playerY, olhandoEsquerda, null, 0f, 0f, false);
        }

        /** Atualiza o drone, regenera O2 e informa quando ele deve disparar. */
        public boolean assist(float delta, float playerX, float playerY, GameSaveData save,
                              float targetX, float targetY, boolean hasTarget) {
            return update(delta, playerX, playerY, false, save, targetX, targetY, hasTarget);
        }

        private boolean update(float delta, float playerX, float playerY, boolean olhandoEsquerda,
                               GameSaveData save, float targetX, float targetY, boolean hasTarget) {
            Vector2 alvo = new Vector2(playerX + (olhandoEsquerda ? 40 : -40), playerY + 30f);
            position.lerp(alvo, Math.min(1f, 4f * delta));
            shotCooldown -= delta;
            muzzleTimer = Math.max(0f, muzzleTimer - delta);

            if (!ativo) return false;

            if (save != null) {
                // O drone recupera O2 continuamente, mas nunca passa do limite.
                save.o2 = Math.min(100f, save.o2 + 2.2f * delta);
            }

            if (hasTarget && shotCooldown <= 0f) {
                shotCooldown = 1.15f;
                muzzleTimer = 0.14f;
                lastShotTarget.set(targetX, targetY);
                return true;
            }
            return false;
        }

        public void loadSprite() {
            if (sprite != null) return;
            try {
                if (Gdx.files.internal("drone.png").exists()) {
                    sprite = new Texture("drone.png");
                    return;
                }
            } catch (Exception ignored) {}

            // Fallback pixel-art caso o projeto ainda nao tenha drone.png.
            Pixmap p = new Pixmap(32, 24, Pixmap.Format.RGBA8888);
            p.setColor(new Color(0.18f, 0.24f, 0.30f, 1f)); p.fillRectangle(7, 7, 18, 10);
            p.setColor(new Color(0.35f, 0.75f, 0.9f, 1f)); p.fillRectangle(10, 4, 12, 3);
            p.setColor(new Color(0.65f, 0.9f, 1f, 1f)); p.fillRectangle(4, 10, 3, 5);
            p.fillRectangle(25, 10, 3, 5);
            p.setColor(new Color(1f, 0.8f, 0.25f, 1f)); p.fillRectangle(14, 10, 4, 3);
            p.setColor(Color.WHITE); p.fillRectangle(9, 16, 4, 2); p.fillRectangle(19, 16, 4, 2);
            sprite = new Texture(p);
            p.dispose();
        }

        public void draw(SpriteBatch batch) {
            if (!ativo || sprite == null) return;
            batch.draw(sprite, position.x, position.y, 42f, 32f);
        }

        public void drawBeam(ShapeRenderer shape) {
            if (!ativo || muzzleTimer <= 0f) return;
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0.35f, 0.9f, 1f, Math.min(1f, muzzleTimer / 0.14f));
            shape.rectLine(position.x + 21f, position.y + 16f, lastShotTarget.x, lastShotTarget.y, 2.5f);
            shape.setColor(Color.WHITE);
            shape.end();
        }

        public void dispose() {
            if (sprite != null) { sprite.dispose(); sprite = null; }
        }
    }

    /** Retorna o inimigo ativo mais proximo do jogador. */
    public static Enemy nearestEnemy(Array<Enemy> enemies, float x, float y) {
        if (enemies == null) return null;
        Enemy nearest = null;
        float best = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e == null || !e.ativo) continue;
            float d2 = Vector2.dst2(x, y, e.rect.x, e.rect.y);
            if (d2 < best) { best = d2; nearest = e; }
        }
        return nearest;
    }

    /** Consome O2; quando chega a zero, a falta de oxigenio passa a retirar vida lentamente. */
    public static void updateOxygen(GameSaveData save, float delta, float drainPerSecond) {
        if (save == null || delta <= 0f) return;
        save.o2 = Math.max(0f, save.o2 - Math.max(0f, drainPerSecond) * delta);
        if (save.o2 <= 0f) {
            save.vida = Math.max(0f, save.vida - 2.5f * delta);
        }
    }

    public static final class Codex {
        public final Set<String> visitados = new LinkedHashSet<>();
        public void visitar(String mundo) { visitados.add(mundo); }
        public boolean aberto(String mundo) { return visitados.contains(mundo); }
        public String texto(String mundo) {
            if (!aberto(mundo)) return "???";
            switch (mundo) {
                case "LUA": return "A base lunar guarda os primeiros sinais da anomalia.";
                case "MARTE": return "As tempestades vermelhas cobrem ruinas de uma antiga colonia.";
                case "TITA": return "O metano de Tita esconde uma rota sob a escuridao.";
                case "CALISTO": return "Fora do cinturao de radiacao, Calisto virou porto logistico.";
                default: return "???";
            }
        }
    }


    public static void criarCadaver(GameSaveData save, float x, float y) {
        if (save == null) return;
        save.cadaverAtivo = true;
        save.cadaverX = x;
        save.cadaverY = y;
        save.cadaverComida = save.inventario.comida;
        save.cadaverMunicao = save.inventario.municao;
        save.cadaverGelo = save.inventario.gelo;
        save.cadaverPeca = save.inventario.peca;
        save.cadaverFiltroO2 = save.inventario.filtroO2;
        save.cadaverDrone = save.inventario.drone;
        save.cadaverNivelArma = save.inventario.nivelArma;
        save.cadaverNivelArmadura = save.inventario.nivelArmadura;
        save.cadaverTemArma = save.inventario.temArma;

        // O jogador perde o conteúdo carregado; tudo fica no corpo salvo.
        save.inventario.comida = 0;
        save.inventario.municao = 0;
        save.inventario.gelo = 0;
        save.inventario.peca = 0;
        save.inventario.filtroO2 = 0;
        save.inventario.drone = 0;
        save.inventario.nivelArma = 0;
        save.inventario.nivelArmadura = 0;
        save.inventario.temArma = false;
        save.municao = 0;
        save.temArma = false;
        save.droneAtivo = false;
    }

    public static boolean recuperarCadaver(GameSaveData save) {
        if (save == null || !save.cadaverAtivo) return false;
        save.inventario.comida += save.cadaverComida;
        save.inventario.municao += save.cadaverMunicao;
        save.inventario.gelo += save.cadaverGelo;
        save.inventario.peca += save.cadaverPeca;
        save.inventario.filtroO2 += save.cadaverFiltroO2;
        save.inventario.drone += save.cadaverDrone;
        save.inventario.nivelArma = Math.max(save.inventario.nivelArma, save.cadaverNivelArma);
        save.inventario.nivelArmadura = Math.max(save.inventario.nivelArmadura, save.cadaverNivelArmadura);
        save.inventario.temArma = save.cadaverTemArma;
        save.municao = save.inventario.municao;
        save.temArma = save.inventario.temArma;
        save.cadaverAtivo = false;
        save.cadaverComida = save.cadaverMunicao = save.cadaverGelo = save.cadaverPeca = save.cadaverFiltroO2 = save.cadaverDrone = 0;
        save.cadaverNivelArma = save.cadaverNivelArmadura = 0;
        save.cadaverTemArma = false;
        save.salvar();
        return true;
    }

    public static boolean craft(Inventario inv, String a, String b, String saida) {
        if (!inv.tem(a) || !inv.tem(b)) return false;
        inv.consumir(a); inv.consumir(b); inv.add(saida); return true;
    }
}
