package com.modulo06.echoesmoon.systems;

/**
 * Progressao de vida dos bosses.
 * Cada boss fica mais resistente conforme bosses anteriores foram derrotados.
 * A ordem de dificuldade final fica: Lua < Marte < Tita < Calisto.
 */
public final class BossBalance {
    private BossBalance() {}

    public static int hpFor(GameSaveData save, int baseHp) {
        int anteriores = 0;
        if (save != null) {
            if (save.bossLuaDerrotado) anteriores++;
            if (save.bossMarteDerrotado) anteriores++;
            if (save.bossTitaDerrotado) anteriores++;
        }
        return Math.max(1, Math.round(baseHp * (1f + anteriores * 0.12f)));
    }
}
