package com.modulo06.echoesmoon.systems;

/**
 * Progressao de upgrades ligada ao numero de inimigos derrotados.
 * Arma: +1 nivel a cada 5 abates, max 3.
 * Armadura: +1 nivel a cada 8 abates, max 3.
 */
public final class UpgradeSystem {
    public static final int MAX_LEVEL = 3;
    public static final int KILLS_PER_WEAPON = 5;
    public static final int KILLS_PER_ARMOR = 8;

    private UpgradeSystem() {}

    public static boolean registerKill(GameSaveData save) {
        if (save == null) return false;
        save.inimigosDerrotados++;
        boolean changed = false;

        int desiredWeapon = Math.min(MAX_LEVEL, save.inimigosDerrotados / KILLS_PER_WEAPON);
        if (desiredWeapon > save.inventario.nivelArma) {
            save.inventario.nivelArma = desiredWeapon;
            changed = true;
            save.ultimoUpgrade = "UPGRADE DE ARMA NV." + desiredWeapon;
        }

        int desiredArmor = Math.min(MAX_LEVEL, save.inimigosDerrotados / KILLS_PER_ARMOR);
        if (desiredArmor > save.inventario.nivelArmadura) {
            save.inventario.nivelArmadura = desiredArmor;
            changed = true;
            save.ultimoUpgrade = "UPGRADE DE ARMADURA NV." + desiredArmor;
        }

        if (changed) save.salvar();
        return changed;
    }

    public static int danoArma(GameSaveData save) {
        int level = save == null || save.inventario == null ? 0 : save.inventario.nivelArma;
        return 10 + 5 * Math.min(MAX_LEVEL, level);
    }

    public static float multiplicadorDanoRecebido(GameSaveData save) {
        int level = save == null || save.inventario == null ? 0 : save.inventario.nivelArmadura;
        return Math.max(0.45f, 1f - 0.15f * Math.min(MAX_LEVEL, level));
    }

    public static float danoRecebido(GameSaveData save, float bruto) {
        return bruto * multiplicadorDanoRecebido(save);
    }

    public static void aplicarDano(GameSaveData save, float bruto) {
        save.o2 -= danoRecebido(save, bruto);
    }

    public static String progresso(GameSaveData save) {
        int kills = save.inimigosDerrotados;
        int proxArma = Math.min(MAX_LEVEL, save.inventario.nivelArma + 1) * KILLS_PER_WEAPON;
        int proxArmadura = Math.min(MAX_LEVEL, save.inventario.nivelArmadura + 1) * KILLS_PER_ARMOR;
        return "ABATES: " + kills + " | PROX. ARMA: " + (save.inventario.nivelArma >= MAX_LEVEL ? "MAX" : proxArma)
                + " | PROX. ARMADURA: " + (save.inventario.nivelArmadura >= MAX_LEVEL ? "MAX" : proxArmadura);
    }
}
