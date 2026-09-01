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

    // Salvar o estado permanentemente no dispositivo
    public void salvar() {
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs");
        prefs.putFloat("playerX", playerX);
        prefs.putFloat("playerY", playerY);
        prefs.putString("fase", fase);
        prefs.putFloat("o2", o2);
        prefs.putFloat("energia", energia);
        prefs.putInteger("municao", municao);
        prefs.putBoolean("temArma", temArma);
        prefs.putBoolean("pecaEstufa", pecaEstufa);
        prefs.putBoolean("repEstufa", repEstufa);
        prefs.putInteger("missaoEtapa", missaoEtapa);
        prefs.flush();
    }

    // Carregar o estado salvo
    public static GameSaveData carregar() {
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs");
        if (!prefs.contains("fase")) return null; // Retorna null se não houver save

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
        return data;
    }

    // Apagar / Resetar o save do dispositivo
    public static void apagar() {
        Preferences prefs = Gdx.app.getPreferences("EchoesMoonPrefs");
        prefs.clear();
        prefs.flush();
    }
}
