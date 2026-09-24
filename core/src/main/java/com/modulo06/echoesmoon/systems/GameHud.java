package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

/** HUD compartilhada. Informacoes secundarias ficam fora da area central de jogo. */
public final class GameHud {
    private static final float HUD_W = 1280f;
    private static final float HUD_H = 720f;
    private static final Matrix4 HUD = new Matrix4();
    private static boolean logAberto = false;
    private static boolean lojaAberta = false;
    private static Texture borderTexture;

    private GameHud() {}

    /** Retorna true enquanto o Codex/Log estiver aberto, pausando a tela. */
    public static boolean handleInput(GameSaveData save) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B) && !logAberto) {
            lojaAberta = !lojaAberta;
            return lojaAberta;
        }
        if (!lojaAberta && Gdx.input.isKeyJustPressed(Input.Keys.L)) logAberto = !logAberto;
        if (lojaAberta) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) comprarArma(save);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) comprarArmadura(save);
            return true;
        }
        return logAberto;
    }

    public static void reset() {
        logAberto = false;
        lojaAberta = false;
    }

    public static boolean isShopAberto() { return lojaAberta; }

    public static boolean isLogAberto() {
        return logAberto;
    }

    public static void draw(ShapeRenderer shape, SpriteBatch batch, BitmapFont font,
                            GameSaveData save, String area, String message) {
        if (save == null) return;

        HUD.setToOrtho2D(0, 0, HUD_W, HUD_H);
        shape.setProjectionMatrix(HUD);
        batch.setProjectionMatrix(HUD);

        // Bloco superior esquerdo: somente o que o jogador realmente precisa durante a luta.
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.52f);
        shape.rect(18, 630, 300, 70);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        drawBar(shape, 30, 675, 135, 9, safeRatio(save.vida, save.vidaMax), new Color(0.88f, 0.18f, 0.18f, 1f));
        drawBar(shape, 30, 653, 135, 9, safeRatio(save.escudo, save.escudoMax), new Color(0.25f, 0.55f, 0.95f, 1f));
        drawBar(shape, 30, 634, 135, 7, safeRatio(save.o2, 100f), new Color(0.15f, 0.78f, 0.92f, 1f));
        shape.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(0.9f);
        font.draw(batch, "VIDA  " + (int)Math.max(0, save.vida) + "/" + (int)Math.max(1, save.vidaMax), 178, 683);
        font.draw(batch, "ESCUDO  " + (int)Math.max(0, save.escudo) + "/" + (int)Math.max(1, save.escudoMax), 178, 662);
        font.draw(batch, "O2  " + (int)Math.max(0, save.o2), 178, 641);

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.82f);
        font.draw(batch, area != null ? area : "", 1010, 688);
        font.draw(batch, "MUNICAO  " + Math.max(0, save.inventario != null ? save.inventario.municao : save.municao), 1010, 665);
        font.draw(batch, "[I] INVENTARIO   [L] CODEX   [B] LOJA", 915, 642);
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.72f);
        font.draw(batch, "DIF: " + (save.dificuldade != null ? save.dificuldade : "NORMAL") + "   CR: " + save.creditos, 1030, 622);

        // Quest fica no rodape para nao disputar espaco com a acao.
        String objetivo = objetivoAtual(save, area);
        if (objetivo != null && !objetivo.isEmpty()) {
            font.getData().setScale(0.86f);
            font.setColor(Color.WHITE);
            font.draw(batch, "OBJETIVO  " + objetivo, 28, 42);
        }

        if (message != null && !message.isEmpty()) {
            font.setColor(Color.WHITE);
            font.getData().setScale(0.92f);
            font.draw(batch, message, 410, 92);
        }
        font.getData().setScale(1f);
        batch.end();

        if (logAberto) drawCodexLog(shape, batch, font, save);
        if (lojaAberta) drawShop(shape, batch, font, save);
        drawCustomBorder(batch);
    }

    public static void drawBoss(ShapeRenderer shape, SpriteBatch batch, BitmapFont font,
                                GameSaveData save, String area, String message,
                                String bossName, float hp, float maxHp,
                                int form, int maxForms, boolean active) {
        draw(shape, batch, font, save, area, message);
        if (!active || logAberto) return;

        float barW = 520f;
        float barH = 18f;
        float barX = (HUD_W - barW) * 0.5f;
        float barY = 669f;
        float ratio = safeRatio(hp, maxHp);

        shape.setProjectionMatrix(HUD);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.02f, 0.02f, 0.03f, 0.80f);
        shape.rect(barX - 8, barY - 26, barW + 16, 60);
        drawBar(shape, barX, barY, barW, barH, ratio, new Color(0.82f, 0.12f, 0.2f, 1f));
        shape.end();

        batch.setProjectionMatrix(HUD);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.05f);
        String label = bossName != null ? bossName : "BOSS";
        if (maxForms > 1) label += "  |  FORMA " + Math.max(1, form) + "/" + maxForms;
        font.draw(batch, label, barX, barY + 38);
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.84f);
        String hpText = "HP " + (int)Math.max(0, hp) + "/" + (int)Math.max(1, maxHp);
        font.draw(batch, hpText, barX + barW - 82, barY - 7);
        font.getData().setScale(1f);
        batch.end();
    }

    private static void drawCodexLog(ShapeRenderer shape, SpriteBatch batch, BitmapFont font,
                                     GameSaveData save) {
        shape.setProjectionMatrix(HUD);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.46f);
        shape.rect(0, 0, HUD_W, HUD_H);
        shape.setColor(0.025f, 0.035f, 0.06f, 0.97f);
        shape.rect(180, 105, 920, 510);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.28f, 0.72f, 0.95f, 1f);
        shape.rect(180, 105, 920, 510);
        shape.rect(210, 160, 500, 410);
        shape.rect(730, 160, 340, 410);
        shape.end();

        batch.setProjectionMatrix(HUD);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.3f);
        font.draw(batch, "CODEX / LOG", 215, 590);
        font.getData().setScale(0.78f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "[L] fechar", 1000, 590);

        font.getData().setScale(1f);
        font.setColor(Color.CYAN);
        font.draw(batch, "REGISTROS", 230, 540);
        font.setColor(Color.WHITE);
        String[] mundos = {"LUA", "MARTE", "TITA", "CALISTO"};
        int y = 505;
        for (String mundo : mundos) {
            font.setColor(save.codex.aberto(mundo) ? Color.WHITE : Color.DARK_GRAY);
            font.draw(batch, mundo + ": " + save.codex.texto(mundo), 230, y);
            y -= 70;
        }

        font.setColor(Color.CYAN);
        font.draw(batch, "QUESTS", 750, 540);
        y = 500;
        for (RecoverySystems.Quest q : save.questLog.quests) {
            font.setColor(q.feita ? Color.GREEN : Color.WHITE);
            font.draw(batch, (q.feita ? "[X] " : "[ ] ") + q.titulo, 750, y);
            y -= 32;
        }
        font.setColor(Color.CYAN);
        font.getData().setScale(1f);
        font.draw(batch, "BESTIARIO", 750, 410);
        font.setColor(Color.WHITE);
        font.getData().setScale(0.78f);
        String[] bestias = {"LUA", "MARTE", "TITA", "CALISTO"};
        boolean[] mortos = {save.bossLuaDerrotado, save.bossMarteDerrotado, save.bossTitaDerrotado, save.bossCalistoDerrotado};
        for (int i = 0; i < bestias.length; i++) {
            font.setColor(mortos[i] ? Color.GREEN : Color.DARK_GRAY);
            font.draw(batch, bestias[i] + ": " + (mortos[i] ? "REGISTRADO" : "???"), 750, 378 - i * 28);
        }
        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(0.8f);
        font.draw(batch, "O jogo fica pausado enquanto o log estiver aberto.", 230, 130);
        font.getData().setScale(1f);
        batch.end();
    }

    private static void comprarArma(GameSaveData save) {
        if (UpgradeSystem.comprarArma(save)) return;
    }

    private static void comprarArmadura(GameSaveData save) {
        if (UpgradeSystem.comprarArmadura(save)) return;
    }

    private static void drawShop(ShapeRenderer shape, SpriteBatch batch, BitmapFont font, GameSaveData save) {
        shape.setProjectionMatrix(HUD);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f,0f,0f,0.48f); shape.rect(0,0,HUD_W,HUD_H);
        shape.setColor(0.025f,0.035f,0.06f,0.98f); shape.rect(300,150,680,420);
        shape.setColor(0.25f,0.72f,0.95f,1f); shape.rect(300,567,680,3);
        shape.end();

        batch.setProjectionMatrix(HUD);
        batch.begin();
        font.setColor(Color.WHITE); font.getData().setScale(1.35f); font.draw(batch, "LOJINHA // MODULOS", 345, 530);
        font.getData().setScale(0.82f); font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Creditos: " + save.creditos, 345, 500);
        font.draw(batch, "[1] UPGRADE DE ARMA     " + nivelTexto(save.inventario.nivelArma, UpgradeSystem.precoArma(Math.min(UpgradeSystem.MAX_LEVEL, save.inventario.nivelArma + 1))), 345, 440);
        font.draw(batch, "[2] UPGRADE DE ARMADURA " + nivelTexto(save.inventario.nivelArmadura, UpgradeSystem.precoArmadura(Math.min(UpgradeSystem.MAX_LEVEL, save.inventario.nivelArmadura + 1))), 345, 385);
        font.setColor(Color.GRAY); font.draw(batch, "Os upgrades nao sao mais liberados por abates.", 345, 315);
        font.draw(batch, "Os inimigos ainda rendem creditos para comprar os modulos.", 345, 290);
        font.draw(batch, "[B] fechar", 850, 205);
        font.getData().setScale(1f);
        batch.end();
    }

    private static String nivelTexto(int nivel, int preco) {
        if (nivel >= UpgradeSystem.MAX_LEVEL) return "MAX";
        return "NV." + nivel + " -> NV." + (nivel + 1) + " | " + preco + " CR";
    }

    private static String objetivoAtual(GameSaveData save, String area) {
        if (area == null) return "";
        switch (area.toUpperCase()) {
            case "LUA":
                return !save.luaPuzzleConcluido ? "complete a ordem das alavancas e recupere as pecas" : (!save.temArma ? "recupere as pecas e fabrique a SlashWave" : "alcance o portal do Guardiao da Lua");
            case "MARTE":
                return save.marteMissoesOk ? "alcance o Titan-Ferrugem" : "sobreviva as ondas e avance pelo setor de Marte";
            case "TITA":
                return save.titaMissoesOk ? "encontre a Besta usando a lanterna" : "atravesse Tita e conserve seu O2";
            case "CALISTO":
                return save.bossTitaDerrotado ? "derrote a Guardia de Calisto" : "alcance Calisto";
            default:
                return "";
        }
    }

    private static void drawCustomBorder(SpriteBatch batch) {
        if (borderTexture == null) {
            try { if (Gdx.files.internal("border.png").exists()) borderTexture = new Texture("border.png"); } catch (Exception ignored) {}
        }
        if (borderTexture == null) return;
        batch.setProjectionMatrix(HUD);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(borderTexture, 0, 0, HUD_W, HUD_H);
        batch.end();
    }

    private static float safeRatio(float value, float max) {
        return max <= 0f ? 0f : MathUtils.clamp(value / max, 0f, 1f);
    }

    private static void drawBar(ShapeRenderer shape, float x, float y, float width, float height,
                                float ratio, Color fill) {
        shape.setColor(0.14f, 0.14f, 0.16f, 1f);
        shape.rect(x, y, width, height);
        shape.setColor(fill);
        shape.rect(x, y, width * MathUtils.clamp(ratio, 0f, 1f), height);
    }
}
