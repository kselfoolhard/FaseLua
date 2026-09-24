package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GameSaveData {
    public float playerX = 100f;
    public float playerY = 100f;
    public String fase = "LUA";
    public float o2 = 100f;
    public float vida = 100f;
    public float vidaMax = 100f;
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
    public int slotId = 1;
    public boolean receitaCraftada = false;
    public float cicloTempestade = 0f;
    public boolean droneAtivo = false;
    public boolean rotaA = false;
    public boolean rotaB = false;
    public float escudo = 50f;
    public float escudoMax = 50f;
    public int creditos = 0;
    public String dificuldade = "NORMAL";

    // Checkpoint / puzzle da Lua.
    public boolean luaPuzzleConcluido = false;

    // Cadaver persistente: guarda equipamento e upgrades perdidos ao morrer.
    public boolean cadaverAtivo = false;
    public float cadaverX = 0f, cadaverY = 0f;
    public int cadaverComida = 0, cadaverMunicao = 0, cadaverGelo = 0, cadaverPeca = 0, cadaverFiltroO2 = 0, cadaverDrone = 0;
    public int cadaverNivelArma = 0, cadaverNivelArmadura = 0;
    public boolean cadaverTemArma = false;
    public RecoverySystems.QuestLog questLog = new RecoverySystems.QuestLog();
    public RecoverySystems.Codex codex = new RecoverySystems.Codex();

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
        salvar(slotId);
    }

    public void salvar(int slot) {
        slotId = slot;
        sincronizarInventario();
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs_slot" + slot);
        prefs.putFloat("playerX", playerX);
        prefs.putFloat("playerY", playerY);
        prefs.putString("fase", fase);
        prefs.putFloat("o2", o2);
        prefs.putFloat("vida", vida);
        prefs.putFloat("vidaMax", vidaMax);
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
        prefs.putInteger("slotId", slotId);
        prefs.putBoolean("receitaCraftada", receitaCraftada);
        prefs.putFloat("cicloTempestade", cicloTempestade);
        prefs.putBoolean("droneAtivo", droneAtivo);
        prefs.putBoolean("rotaA", rotaA);
        prefs.putBoolean("rotaB", rotaB);
        prefs.putFloat("escudo", escudo);
        prefs.putFloat("escudoMax", escudoMax);
        prefs.putInteger("creditos", creditos);
        prefs.putString("dificuldade", dificuldade);
        prefs.putBoolean("luaPuzzleConcluido", luaPuzzleConcluido);
        prefs.putBoolean("cadaverAtivo", cadaverAtivo);
        prefs.putFloat("cadaverX", cadaverX);
        prefs.putFloat("cadaverY", cadaverY);
        prefs.putInteger("cadaverComida", cadaverComida);
        prefs.putInteger("cadaverMunicao", cadaverMunicao);
        prefs.putInteger("cadaverGelo", cadaverGelo);
        prefs.putInteger("cadaverPeca", cadaverPeca);
        prefs.putInteger("cadaverFiltroO2", cadaverFiltroO2);
        prefs.putInteger("cadaverDrone", cadaverDrone);
        prefs.putInteger("cadaverNivelArma", cadaverNivelArma);
        prefs.putInteger("cadaverNivelArmadura", cadaverNivelArmadura);
        prefs.putBoolean("cadaverTemArma", cadaverTemArma);
        prefs.putInteger("gelo", inventario.gelo);
        prefs.putInteger("peca", inventario.peca);
        prefs.putInteger("filtroO2", inventario.filtroO2);
        prefs.putInteger("drone", inventario.drone);
        for (int i = 0; i < questLog.quests.size; i++) prefs.putBoolean("quest_" + i, questLog.quests.get(i).feita);
        for (String mundo : new String[]{"LUA", "MARTE", "TITA", "CALISTO"}) prefs.putBoolean("codex_" + mundo, codex.aberto(mundo));
        prefs.flush();
    }

    public static GameSaveData carregar() {
        return carregar(1);
    }

    public static GameSaveData carregar(int slot) {
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs_slot" + slot);
        if (!prefs.contains("fase")) return null;

        GameSaveData data = new GameSaveData();
        data.playerX = prefs.getFloat("playerX", 100f);
        data.playerY = prefs.getFloat("playerY", 100f);
        data.fase = prefs.getString("fase", "LUA");
        data.o2 = prefs.getFloat("o2", 100f);
        data.vida = prefs.getFloat("vida", 100f);
        data.vidaMax = prefs.getFloat("vidaMax", 100f);
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
        data.slotId = slot;
        data.receitaCraftada = prefs.getBoolean("receitaCraftada", false);
        data.cicloTempestade = prefs.getFloat("cicloTempestade", 0f);
        data.droneAtivo = prefs.getBoolean("droneAtivo", false);
        data.rotaA = prefs.getBoolean("rotaA", false);
        data.rotaB = prefs.getBoolean("rotaB", false);
        data.escudo = prefs.getFloat("escudo", 50f);
        data.escudoMax = prefs.getFloat("escudoMax", 50f);
        data.creditos = prefs.getInteger("creditos", 0);
        data.dificuldade = prefs.getString("dificuldade", "NORMAL");
        data.luaPuzzleConcluido = prefs.getBoolean("luaPuzzleConcluido", false);
        data.cadaverAtivo = prefs.getBoolean("cadaverAtivo", false);
        data.cadaverX = prefs.getFloat("cadaverX", 0f);
        data.cadaverY = prefs.getFloat("cadaverY", 0f);
        data.cadaverComida = prefs.getInteger("cadaverComida", 0);
        data.cadaverMunicao = prefs.getInteger("cadaverMunicao", 0);
        data.cadaverGelo = prefs.getInteger("cadaverGelo", 0);
        data.cadaverPeca = prefs.getInteger("cadaverPeca", 0);
        data.cadaverFiltroO2 = prefs.getInteger("cadaverFiltroO2", 0);
        data.cadaverDrone = prefs.getInteger("cadaverDrone", 0);
        data.cadaverNivelArma = prefs.getInteger("cadaverNivelArma", 0);
        data.cadaverNivelArmadura = prefs.getInteger("cadaverNivelArmadura", 0);
        data.cadaverTemArma = prefs.getBoolean("cadaverTemArma", false);
        data.inventario.gelo = prefs.getInteger("gelo", 0);
        data.inventario.peca = prefs.getInteger("peca", 0);
        data.inventario.filtroO2 = prefs.getInteger("filtroO2", 0);
        data.inventario.drone = prefs.getInteger("drone", 0);
        for (int i = 0; i < data.questLog.quests.size; i++) data.questLog.quests.get(i).feita = prefs.getBoolean("quest_" + i, false);
        for (String mundo : new String[]{"LUA", "MARTE", "TITA", "CALISTO"}) if (prefs.getBoolean("codex_" + mundo, false)) data.codex.visitar(mundo);
        data.sincronizarInventario();
        return data;
    }

    public static void apagar() {
        for (int slot = 1; slot <= 2; slot++) {
            Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs_slot" + slot);
            prefs.clear(); prefs.flush();
        }
    }
}
