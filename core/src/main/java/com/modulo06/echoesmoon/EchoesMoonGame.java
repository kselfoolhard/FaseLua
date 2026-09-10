package com.modulo06.echoesmoon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.modulo06.echoesmoon.screens.IntroScreen;
import com.modulo06.echoesmoon.screens.MenuScreen;
import com.modulo06.echoesmoon.systems.GameSaveData;
import com.modulo06.echoesmoon.systems.SoundManager;

public class EchoesMoonGame extends Game {

    // NOVO: AssetManager global do jogo
    public AssetManager assets;

    @Override
    public void create() {
        SoundManager.carregarSonsPadrao(); // << CARREGA OS SONS NA MEMÓRIA
        setScreen(new IntroScreen(this, new GameSaveData()));
    }

    @Override
    public void dispose() {
        super.dispose();
        // Libera a memória de todos os sprites quando o jogo fecha
        if (assets != null) {
            assets.dispose();
        }
    }
}
