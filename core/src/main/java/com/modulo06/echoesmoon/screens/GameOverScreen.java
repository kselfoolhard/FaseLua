package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.modulo06.echoesmoon.systems.GameSaveData;

/**
 * Tela de morte agora funciona como uma cutscene.
 * Coloque assets/video/earthdestroyed.webm no projeto.
 */
public class GameOverScreen extends VideoCutsceneScreen {
    public GameOverScreen(Game game) {
        super(game, "video/earthdestroyed.webm", new GameOverFallbackScreen(game), false);
    }

    /** Mantem a opcao de voltar ao menu/carregar o save caso o video falhe. */
    private static class GameOverFallbackScreen extends MenuScreen {
        public GameOverFallbackScreen(Game game) { super(game); }
    }
}
