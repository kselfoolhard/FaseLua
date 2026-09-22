package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSaveData {
    public float playerX = 100f;
    public float playerY = 100f;
    public String fase = "LUA";
    public float o2 = 100f;
    public float energia = 100f;
    public int municao = 10;

    public boolean temArma = false;
    public boolean pecaEstufa = false;
    public boolean repEstufa = false;
    public int missaoEtapa = 0;

    // Flags da campanha da Parte 3.
    public boolean luaMissoesOk = false;
    public boolean marteMissoesOk = false;
    public boolean titaMissoesOk = false;
    public boolean bossLuaDerrotado = false;
    public boolean bossMarteDerrotado = false;
    public boolean bossTitaDerrotado = false;
    public boolean bossCalistoDerrotado = false;

    // Final alternativo: ativado ao encontrar as bancadas escondidas (disfarcadas de
    // gelo, sprite ice.png) espalhadas pelos mapas. Uma por mundo.
    public boolean rotaEstranha = false;
    public boolean segredoLuaEncontrado = false;
    public boolean segredoMarteEncontrado = false;
    public boolean segredoTitaEncontrado = false;
    public boolean segredoCalistoEncontrado = false;

    // Progressao de upgrades.
    public int inimigosDerrotados = 0;
    public String ultimoUpgrade = "";

    public Inventario inventario = new Inventario();

    public GameSaveData() {
        sincronizarInventario();
    }

    /** Mantem os campos antigos e o inventario jogando junto. */
    public void sincronizarInventario() {
        if (inventario == null) inventario = new Inventario();
        inventario.municao = Math.max(inventario.municao, municao);
        municao = inventario.municao;
        inventario.temArma = inventario.temArma || temArma;
        temArma = inventario.temArma;
    }

    public void salvar() {
        sincronizarInventario();
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs");
        prefs.putFloat("playerX", playerX);
        prefs.putFloat("playerY", playerY);
        prefs.putString("fase", fase);
        prefs.putFloat("o2", o2);
        prefs.putFloat("energia", energia);
        prefs.putInteger("municao", inventario.municao);
        prefs.putBoolean("temArma", inventario.temArma);
        prefs.putBoolean("pecaEstufa", pecaEstufa);
        prefs.putBoolean("repEstufa", repEstufa);
        prefs.putInteger("missaoEtapa", missaoEtapa);

        prefs.putInteger("comida", inventario.comida);
        prefs.putInteger("nivelArma", inventario.nivelArma);
        prefs.putInteger("nivelArmadura", inventario.nivelArmadura);
        prefs.putBoolean("chaveLua", inventario.chaveLua);
        prefs.putBoolean("chaveMarte", inventario.chaveMarte);
        prefs.putBoolean("chaveTita", inventario.chaveTita);
        prefs.putBoolean("chaveLuz", inventario.chaveLuz);

        prefs.putBoolean("luaMissoesOk", luaMissoesOk);
        prefs.putBoolean("marteMissoesOk", marteMissoesOk);
        prefs.putBoolean("titaMissoesOk", titaMissoesOk);
        prefs.putBoolean("bossLuaDerrotado", bossLuaDerrotado);
        prefs.putBoolean("bossMarteDerrotado", bossMarteDerrotado);
        prefs.putBoolean("bossTitaDerrotado", bossTitaDerrotado);
        prefs.putBoolean("bossCalistoDerrotado", bossCalistoDerrotado);
        prefs.putBoolean("rotaEstranha", rotaEstranha);
        prefs.putBoolean("segredoLuaEncontrado", segredoLuaEncontrado);
        prefs.putBoolean("segredoMarteEncontrado", segredoMarteEncontrado);
        prefs.putBoolean("segredoTitaEncontrado", segredoTitaEncontrado);
        prefs.putBoolean("segredoCalistoEncontrado", segredoCalistoEncontrado);
        prefs.putInteger("inimigosDerrotados", inimigosDerrotados);
        prefs.putString("ultimoUpgrade", ultimoUpgrade);
        prefs.flush();
    }

    public static GameSaveData carregar() {
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs");
        if (!prefs.contains("fase")) return null;

        GameSaveData data = new GameSaveData();
        data.playerX = prefs.getFloat("playerX", 100f);
        data.playerY = prefs.getFloat("playerY", 100f);
        data.fase = prefs.getString("fase", "LUA");
        data.o2 = prefs.getFloat("o2", 100f);
        data.energia = prefs.getFloat("energia", 100f);
        data.municao = prefs.getInteger("municao", 10);
        data.temArma = prefs.getBoolean("temArma", false);
        data.pecaEstufa = prefs.getBoolean("pecaEstufa", false);
        data.repEstufa = prefs.getBoolean("repEstufa", false);
        data.missaoEtapa = prefs.getInteger("missaoEtapa", 0);

        data.inventario.comida = prefs.getInteger("comida", 0);
        data.inventario.municao = data.municao;
        data.inventario.nivelArma = prefs.getInteger("nivelArma", 0);
        data.inventario.nivelArmadura = prefs.getInteger("nivelArmadura", 0);
        data.inventario.temArma = prefs.getBoolean("temArma", false);
        data.inventario.chaveLua = prefs.getBoolean("chaveLua", false);
        data.inventario.chaveMarte = prefs.getBoolean("chaveMarte", false);
        data.inventario.chaveTita = prefs.getBoolean("chaveTita", false);
        data.inventario.chaveLuz = prefs.getBoolean("chaveLuz", false);

        data.luaMissoesOk = prefs.getBoolean("luaMissoesOk", false);
        data.marteMissoesOk = prefs.getBoolean("marteMissoesOk", false);
        data.titaMissoesOk = prefs.getBoolean("titaMissoesOk", false);
        data.bossLuaDerrotado = prefs.getBoolean("bossLuaDerrotado", false);
        data.bossMarteDerrotado = prefs.getBoolean("bossMarteDerrotado", false);
        data.bossTitaDerrotado = prefs.getBoolean("bossTitaDerrotado", false);
        data.bossCalistoDerrotado = prefs.getBoolean("bossCalistoDerrotado", false);
        data.rotaEstranha = prefs.getBoolean("rotaEstranha", false);
        data.segredoLuaEncontrado = prefs.getBoolean("segredoLuaEncontrado", false);
        data.segredoMarteEncontrado = prefs.getBoolean("segredoMarteEncontrado", false);
        data.segredoTitaEncontrado = prefs.getBoolean("segredoTitaEncontrado", false);
        data.segredoCalistoEncontrado = prefs.getBoolean("segredoCalistoEncontrado", false);
        data.inimigosDerrotados = prefs.getInteger("inimigosDerrotados", 0);
        data.ultimoUpgrade = prefs.getString("ultimoUpgrade", "");
        data.sincronizarInventario();
        return data;
    }

    public static void apagar() {
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs");
        prefs.clear();
        prefs.flush();
    }
}
