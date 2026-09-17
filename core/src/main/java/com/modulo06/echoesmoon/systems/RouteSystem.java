package com.modulo06.echoesmoon.systems;

/** Regras da rota alternativa de final. */
public final class RouteSystem {
    /**
     * A rota exige uma progressao claramente acima do normal.
     * 100 abates + arma e armadura no nivel maximo.
     */
    public static final int KILLS_THRESHOLD = 100;

    private RouteSystem() {}

    public static boolean isExtremelyStrong(GameSaveData save) {
        if (save == null || save.inventario == null) return false;

        return save.inimigosDerrotados >= KILLS_THRESHOLD
                && save.inventario.nivelArma >= UpgradeSystem.MAX_LEVEL
                && save.inventario.nivelArmadura >= UpgradeSystem.MAX_LEVEL;
    }

    /** Ativa a rota quando o jogador chega ao requisito. */
    public static boolean tryActivate(GameSaveData save) {
        if (save == null) return false;
        if (save.rotaEstranha) return true;
        if (!isExtremelyStrong(save)) return false;

        save.rotaEstranha = true;
        save.salvar();
        return true;
    }

    /** Alias para compatibilidade com telas antigas. */
    public static boolean updateAndCheck(GameSaveData save) {
        return tryActivate(save);
    }

    public static boolean isActive(GameSaveData save) {
        return save != null && save.rotaEstranha;
    }
}
