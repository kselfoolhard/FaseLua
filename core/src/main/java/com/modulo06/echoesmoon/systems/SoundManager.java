package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;

public class SoundManager {
    private static HashMap<String, Sound> sounds = new HashMap<>();
    private static HashMap<String, Music> musics = new HashMap<>();
    private static Music currentMusic = null;

    public static void loadSound(String key, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(key, Gdx.audio.newSound(Gdx.files.internal(path)));
            }
        } catch (Exception ignored) {}
    }

    public static void loadMusic(String key, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                musics.put(key, Gdx.audio.newMusic(Gdx.files.internal(path)));
            }
        } catch (Exception ignored) {}
    }

    public static void playSound(String key) {
        if (sounds.containsKey(key)) {
            sounds.get(key).play(0.8f);
        }
    }

    public static void playMusic(String key, boolean loop) {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
        if (musics.containsKey(key)) {
            currentMusic = musics.get(key);
            currentMusic.setLooping(loop);
            currentMusic.setVolume(0.5f);
            currentMusic.play();
        }
    }

    public static void dispose() {
        for (Sound s : sounds.values()) s.dispose();
        for (Music m : musics.values()) m.dispose();
        sounds.clear();
        musics.clear();
    }
}
