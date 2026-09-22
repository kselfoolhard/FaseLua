package com.modulo06.echoesmoon.systems;

/**
 * Regras da rota alternativa de final.
 *
 * Antes exigia uma grindagem extrema (100 abates + arma/armadura no maximo).
 * Agora a rota estranha e ativada encontrando as 4 bancadas escondidas
 * (disfarcadas de gelo, sprite ice.png) espalhadas pelos mapas de Lua, Marte,
 * Tita e Calisto. Sao bem sutis: o jogador precisa clicar nelas para revelar
 * o segredo.
 */
public final class RouteSystem {
    /** Quantas bancadas escondidas existem no total (uma por mundo). */
    public static final int TOTAL_SEGREDOS = 4;

    private RouteSystem() {}

    public static int segredosEncontrados(GameSaveData save) {
        if (save == null) return 0;
        int total = 0;
        if (save.segredoLuaEncontrado) total++;
        if (save.segredoMarteEncontrado) total++;
        if (save.segredoTitaEncontrado) total++;
        if (save.segredoCalistoEncontrado) total++;
        return total;
    }

    public static boolean todosSegredosEncontrados(GameSaveData save) {
        return segredosEncontrados(save) >= TOTAL_SEGREDOS;
    }

    /** Ativa a rota quando todas as bancadas escondidas foram encontradas. */
    public static boolean tryActivate(GameSaveData save) {
        if (save == null) return false;
        if (save.rotaEstranha) return true;
        if (!todosSegredosEncontrados(save)) return false;

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
