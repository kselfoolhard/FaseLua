package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

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
        if (id.equals("FILTRO_O2")) return filtroO2 > 0;
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
        else if (id.equals("FILTRO_O2")) filtroO2++;
        else if (id.equals("DRONE")) drone++;
    }

    public boolean consumir(String id) {
        if (!tem(id)) return false;
        if (id.equals("GELO")) gelo--; else if (id.equals("PECA")) peca--;
        else if (id.equals("FILTRO_O2")) filtroO2--; else if (id.equals("DRONE")) drone--;
        return true;
    }

    /** Comida recupera um pouco menos que um pickup de O2 (12 versus 20). */
    public boolean usarComida(GameSaveData save) {
        if (comida <= 0) return false;
        comida--;
        save.o2 = Math.min(save.o2 + 12f, 100f);
        save.salvar();
        return true;
    }

    public void render(SpriteBatch batch, BitmapFont font, Matrix4 hudProjection) {
        render(batch, font, hudProjection, null);
    }

    public void render(SpriteBatch batch, BitmapFont font, Matrix4 hudProjection, GameSaveData save) {
        if (!aberto) return;
        batch.setProjectionMatrix(hudProjection);
        batch.begin();
        font.getData().setScale(1.05f);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "=== INVENTARIO (I fecha | C usa comida) ===", 40, 660);
        font.draw(batch, "Comida: " + comida + "   (+12 O2)", 60, 620);
        font.draw(batch, "Municao: " + municao, 60, 590);
        font.draw(batch, "Gelo: " + gelo + " | Pecas: " + peca + " | Filtros: " + filtroO2, 60, 575);
        font.draw(batch, "Drone: " + (drone > 0 ? "DISPONIVEL" : "NAO"), 60, 545);
        int dano = save != null ? UpgradeSystem.danoArma(save) : (10 + 5 * Math.min(3, nivelArma));
        int reducao = save != null ? Math.round((1f - UpgradeSystem.multiplicadorDanoRecebido(save)) * 100f) : (15 * Math.min(3, nivelArmadura));
        font.draw(batch, "Arma NV." + nivelArma + " | DANO " + dano, 60, 515);
        font.draw(batch, "Armadura NV." + nivelArmadura + " | REDUCAO " + reducao + "%", 60, 485);
        font.draw(batch, "Inimigos derrotados: " + (save != null ? save.inimigosDerrotados : "-"), 60, 500);
        font.draw(batch, "Chave Lua: " + (chaveLua ? "SIM" : "NAO"), 60, 450);
        font.draw(batch, "Chave Marte: " + (chaveMarte ? "SIM" : "NAO"), 60, 420);
        font.draw(batch, "Chave Tita: " + (chaveTita ? "SIM" : "NAO"), 60, 390);
        font.draw(batch, "Chave Luz: " + (chaveLuz ? "SIM" : "NAO"), 60, 360);
        if (save != null && !save.ultimoUpgrade.isEmpty()) {
            font.draw(batch, save.ultimoUpgrade, 60, 320);
        }
        font.getData().setScale(1f);
        batch.end();
    }

}
