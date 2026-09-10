package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class SoundManager {
    private static HashMap<String, Sound> sounds = new HashMap<>();
    private static HashMap<String, Music> musics = new HashMap<>();
    private static Music currentMusic = null;

    public static void carregarSonsPadrao() {
        // SFX (Pasta sfx)
        loadSound("bossgrowl", "sfx/bossgrowl.wav");
        loadSound("enemy", "sfx/enemy.wav");
        loadSound("footstep1", "sfx/footstep1.wav");
        loadSound("footstep2", "sfx/footstep2.wav");
        loadSound("hit_enemy", "sfx/hit_enemy.wav");
        loadSound("hit_player", "sfx/hit_player.wav");
        loadSound("slash", "sfx/slashwave_slash.wav");

        // Músicas (Pasta music)
        loadMusic("boss", "music/boss.ogg");
        loadMusic("horda", "music/horda.ogg");
        loadMusic("lua", "music/lua.ogg");
        loadMusic("marte", "music/marte.ogg");
    }

    public static void loadSound(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            sounds.put(key, Gdx.audio.newSound(Gdx.files.internal(path)));
        } else {
            System.out.println("FALTOU O SOM: " + path);
        }
    }

    public static void loadMusic(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            musics.put(key, Gdx.audio.newMusic(Gdx.files.internal(path)));
        } else {
            System.out.println("FALTOU A MUSICA: " + path);
        }
    }

    public static void playSound(String key) {
        if (sounds.containsKey(key)) sounds.get(key).play(1.0f);
    }

    public static void playMusic(String key, boolean loop) {
        if (currentMusic != null) currentMusic.stop();
        if (musics.containsKey(key)) {
            currentMusic = musics.get(key);
            currentMusic.setLooping(loop);
            currentMusic.setVolume(0.5f);
            currentMusic.play();
        }
    }

    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) currentMusic.stop();
    }
}
