package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.Enemy;
import com.modulo06.echoesmoon.entities.SlashWave;

public class CombatSystem {

    // Processa colisões de tiros, dano nos inimigos e drops de O2
    public static void processarCombate(Array<SlashWave> slashes, Array<Enemy> enemies, GameSaveData saveData) {
        for (int i = slashes.size - 1; i >= 0; i--) {
            SlashWave s = slashes.get(i);

            if (!s.active) {
                slashes.removeIndex(i);
                continue;
            }

            for (Enemy e : enemies) {
                if (e.ativo && s.rect.overlaps(e.rect)) {
                    e.hp -= 40;
                    s.active = false;

                    // Lógica de Morte do Inimigo
                    if (e.hp <= 0) {
                        e.ativo = false;

                        // Drop aleatório de O2 (40% de chance de recuperar 10 de O2)
                        if (MathUtils.randomBoolean(0.4f)) {
                            saveData.o2 = Math.min(100, saveData.o2 + 10);
                        }
                    }
                    break;
                }
            }

            if (!s.active) {
                slashes.removeIndex(i);
            }
        }
    }
}
