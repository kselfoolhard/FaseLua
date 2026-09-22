package com.modulo06.echoesmoon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.modulo06.echoesmoon.screens.IntroScreen;
import com.modulo06.echoesmoon.systems.CrosshairUtil;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SoundManager;

public class EchoesMoonGame extends Game {
    public AssetManager assets;

    @Override
    public void create() {
        CrosshairUtil.esconderCursorDoSistema();
        SoundManager.carregarSonsPadrao();
        GameSaveData save = GameSaveData.carregar();
        if (save == null) save = new GameSaveData();
        else save.sincronizarInventario();
        setScreen(new IntroScreen(this, save));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assets != null) assets.dispose();
    }
}
