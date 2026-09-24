package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public final class SoundManager {
    private static final HashMap<String, Sound> sounds = new HashMap<>();
    private static final HashMap<String, Music> musics = new HashMap<>();
    private static Music currentMusic = null;

    private SoundManager() {}

    public static void carregarSonsPadrao() {
        loadSound("bossgrowl", "sfx/bossgrowl.wav");
        loadSound("enemy", "sfx/enemy.wav");
        loadSound("footstep1", "sfx/footstep1.wav");
        loadSound("footstep2", "sfx/footstep2.wav");
        loadSound("hit_enemy", "sfx/hit_enemy.wav");
        loadSound("hit_player", "sfx/hit_player.wav");
        loadSound("slash", "sfx/slashwave_slash.wav");
        loadSound("pickup", "sfx/pickup.wav");
        loadSoundCandidates("punch", "sfx/punch.wav", "sfx/soco.wav", "punch.wav", "soco.wav");
        loadSoundCandidates("charged", "sfx/charged.wav", "charged.wav");
        loadSound("reload", "sfx/reload.wav");

        loadMusicCandidates("boss", "music/boss.ogg", "music/boss.mp3", "music/boss.wav");
        loadMusicCandidates("tita", "boss_tita.ogg", "music/boss_tita.ogg", "music/boss_tita.mp3", "music/tita.ogg", "music/tita.mp3");
        loadMusicCandidates("horda", "music/horda.ogg", "music/horda.mp3");
        loadMusicCandidates("lua", "music/lua.ogg", "music/lua.mp3");
        loadMusicCandidates("marte", "music/marte.ogg", "music/marte.mp3");
    }

    public static void loadSound(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            sounds.put(key, Gdx.audio.newSound(Gdx.files.internal(path)));
        } else {
            Gdx.app.log("SoundManager", "FALTOU O SOM: " + path);
        }
    }

    public static void loadMusic(String key, String path) {
        if (Gdx.files.internal(path).exists()) {
            musics.put(key, Gdx.audio.newMusic(Gdx.files.internal(path)));
        } else {
            Gdx.app.log("SoundManager", "FALTOU A MUSICA: " + path);
        }
    }

    private static void loadSoundCandidates(String key, String... paths) {
        for (String path : paths) {
            if (Gdx.files.internal(path).exists()) {
                loadSound(key, path);
                return;
            }
        }
        Gdx.app.log("SoundManager", "NENHUM SOM ENCONTRADO PARA: " + key);
    }

    private static void loadMusicCandidates(String key, String... paths) {
        for (String path : paths) {
            if (Gdx.files.internal(path).exists()) {
                loadMusic(key, path);
                return;
            }
        }
        Gdx.app.log("SoundManager", "NENHUMA MUSICA ENCONTRADA PARA: " + key);
    }

    public static void playSound(String key) {
        Sound sound = sounds.get(key);
        if (sound != null) {
            float volume = key.equals("slash") ? 0.3f : 1.0f;
            sound.play(volume);
        }
    }

    public static void playMusic(String key, boolean loop) {
        if (currentMusic != null) currentMusic.stop();

        Music music = musics.get(key);

        // Tenta carregar a trilha novamente caso ela tenha sido adicionada depois
        // da inicializacao do jogo. Tita nunca usa a trilha generica de boss.
        if (music == null) {
            if ("tita".equals(key)) {
                String[] candidates = {
                    "boss_tita.ogg",
                    "music/boss_tita.ogg",
                    "sounds/boss_tita.ogg",
                    "audio/boss_tita.ogg"
                };
                for (String path : candidates) {
                    if (Gdx.files.internal(path).exists()) {
                        loadMusic("tita", path);
                        music = musics.get("tita");
                        break;
                    }
                }
            }
        }

        if (music == null) {
            Gdx.app.log("SoundManager", "MUSICA NAO ENCONTRADA: " + key);
            currentMusic = null;
            return;
        }

        currentMusic = music;
        currentMusic.stop();
        currentMusic.setPosition(0f);
        currentMusic.setLooping(loop);
        currentMusic.setVolume(0.5f);
        currentMusic.play();
    }

    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) currentMusic.stop();
    }
}
