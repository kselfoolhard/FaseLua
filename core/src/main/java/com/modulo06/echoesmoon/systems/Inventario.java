package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

/** Inventario visual compartilhado por todas as telas. */
public class Inventario {
    public int comida = 0;
    public int municao = 0;
    public int gelo = 0;
    public int peca = 0;
    public int filtroO2 = 0;
    public int drone = 0;
    public int nivelArma = 0;
    public int nivelArmadura = 0;

    public boolean temArma = false;
    public boolean chaveLua = false;
    public boolean chaveMarte = false;
    public boolean chaveTita = false;
    public boolean chaveLuz = false;
    public boolean aberto = false;

    public boolean tem(String id) {
        if (id.equals("CHAVE_LUA")) return chaveLua;
        if (id.equals("CHAVE_MARTE")) return chaveMarte;
        if (id.equals("CHAVE_TITA")) return chaveTita;
        if (id.equals("CHAVE_LUZ")) return chaveLuz;
        if (id.equals("GELO")) return gelo > 0;
        if (id.equals("PECA")) return peca > 0;
        if (id.equals("DRONE")) return drone > 0;
        return false;
    }

    public void add(String id) {
        if (id.equals("CHAVE_LUA")) chaveLua = true;
        else if (id.equals("CHAVE_MARTE")) chaveMarte = true;
        else if (id.equals("CHAVE_TITA")) chaveTita = true;
        else if (id.equals("CHAVE_LUZ")) chaveLuz = true;
        else if (id.equals("COMIDA")) comida++;
        else if (id.equals("GELO")) gelo++;
        else if (id.equals("PECA")) peca++;
        else if (id.equals("DRONE")) drone++;
    }

    public boolean consumir(String id) {
        if (!tem(id)) return false;
        if (id.equals("GELO")) gelo--;
        else if (id.equals("PECA")) peca--;
        else if (id.equals("DRONE")) drone--;
        return true;
    }

    /** Comida regenera 25 HP. */
    public boolean usarComida(GameSaveData save) {
        if (comida <= 0 || save == null) return false;
        comida--;
        save.vida = Math.min(save.vida + 25f, save.vidaMax);
        save.salvar();
        return true;
    }

    public void render(SpriteBatch batch, ShapeRenderer shape, BitmapFont font, Matrix4 hudProjection, GameSaveData save) {
        if (!aberto) return;

        batch.setProjectionMatrix(hudProjection);
        shape.setProjectionMatrix(hudProjection);

        final float x = 320f, y = 145f, w = 640f, h = 430f;

        // Fundo translucido: o mapa continua visivel pelas bordas.
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.38f);
        shape.rect(0, 0, 1280, 720);
        shape.setColor(0.025f, 0.035f, 0.06f, 0.96f);
        shape.rect(x, y, w, h);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.2f, 0.72f, 0.95f, 1f);
        shape.rect(x, y, w, h);
        shape.setColor(0.18f, 0.24f, 0.32f, 1f);
        shape.rect(x + 20, y + 250, 290, 120);
        shape.rect(x + 330, y + 250, 290, 120);
        shape.rect(x + 20, y + 90, 290, 135);
        shape.rect(x + 330, y + 90, 290, 135);
        shape.end();

        int dano = save != null ? UpgradeSystem.danoArma(save) : (10 + 5 * Math.min(3, nivelArma));
        int reducao = save != null ? Math.round((1f - UpgradeSystem.multiplicadorDanoRecebido(save)) * 100f) : (15 * Math.min(3, nivelArmadura));

        batch.begin();
        float oldX = font.getData().scaleX, oldY = font.getData().scaleY;
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.draw(batch, "INVENTARIO", x + 28, y + h - 28);
        font.getData().setScale(0.8f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "[I] fechar   [C] usar racao", x + w - 185, y + h - 31);

        font.getData().setScale(0.9f);
        font.setColor(Color.CYAN);
        font.draw(batch, "RECURSOS", x + 32, y + 336);
        font.setColor(Color.WHITE);
        font.draw(batch, "Racoes: " + comida + "   (+25 VIDA)", x + 32, y + 306);
        font.draw(batch, "Municao: " + municao, x + 32, y + 278);

        font.setColor(Color.CYAN);
        font.draw(batch, "EQUIPAMENTO", x + 342, y + 336);
        font.setColor(Color.WHITE);
        font.draw(batch, "Arma NV." + nivelArma + "   Dano: " + dano, x + 342, y + 306);
        font.draw(batch, "Armadura NV." + nivelArmadura + "   Reducao: " + reducao + "%", x + 342, y + 278);

        font.setColor(Color.CYAN);
        font.draw(batch, "MATERIAIS", x + 32, y + 198);
        font.setColor(Color.WHITE);
        font.draw(batch, "Gelo: " + gelo, x + 32, y + 170);
        font.draw(batch, "Pecas: " + peca, x + 32, y + 146);
        font.draw(batch, "Drone: " + (drone > 0 ? "ONLINE" : "OFFLINE"), x + 32, y + 122);

        font.setColor(Color.CYAN);
        font.draw(batch, "CREDENCIAIS", x + 342, y + 198);
        font.setColor(Color.WHITE);
        font.draw(batch, "Lua: " + acesso(chaveLua), x + 342, y + 170);
        font.draw(batch, "Marte: " + acesso(chaveMarte), x + 342, y + 146);
        font.draw(batch, "Tita: " + acesso(chaveTita), x + 342, y + 122);
        font.draw(batch, "Luz: " + acesso(chaveLuz), x + 342, y + 98);

        if (save != null && !save.ultimoUpgrade.isEmpty()) {
            font.setColor(Color.GREEN);
            font.getData().setScale(0.78f);
            font.draw(batch, "Ultimo upgrade: " + save.ultimoUpgrade, x + 24, y + 52);
        }
        font.setColor(Color.GRAY);
        font.getData().setScale(0.72f);
        font.draw(batch, "Creditos: " + (save != null ? save.creditos : 0), x + 390, y + 52);
        font.getData().setScale(oldX, oldY);
        batch.end();
    }

    private String acesso(boolean ativo) {
        return ativo ? "LIBERADO" : "BLOQUEADO";
    }

}
