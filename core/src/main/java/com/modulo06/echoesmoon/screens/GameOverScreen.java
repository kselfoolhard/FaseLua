package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Game;

public class GameOverScreen extends VideoCutsceneScreen {
    public GameOverScreen(Game game) {
        super(game, "video/earthdestroyed.webm", new GameOverFallbackScreen(game));
    }

    /** Mantem a opcao de voltar ao menu/carregar o save caso o video falhe. */
    private static class GameOverFallbackScreen extends MenuScreen {
        public GameOverFallbackScreen(Game game) {
            super(game);
        }
    }
}
