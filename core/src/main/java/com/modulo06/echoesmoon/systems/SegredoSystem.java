package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.Rectangle;

/**
 * Logica compartilhada das bancadas escondidas (disfarcadas de gelo, sprite
 * ice.png) espalhadas pelos mapas. Encontrar as 4 (uma por mundo) ativa a
 * rota estranha / final alternativo, atraves do RouteSystem.
 */
public final class SegredoSystem {
    public static final String LUA = "LUA";
    public static final String MARTE = "MARTE";
    public static final String TITA = "TITA";
    public static final String CALISTO = "CALISTO";

    private SegredoSystem() {}

    public static boolean jaEncontrado(GameSaveData save, String mundo) {
        if (save == null) return true;
        switch (mundo) {
            case LUA: return save.segredoLuaEncontrado;
            case MARTE: return save.segredoMarteEncontrado;
            case TITA: return save.segredoTitaEncontrado;
            case CALISTO: return save.segredoCalistoEncontrado;
            default: return true;
        }
    }

    private static void marcarEncontrado(GameSaveData save, String mundo) {
        switch (mundo) {
            case LUA: save.segredoLuaEncontrado = true; break;
            case MARTE: save.segredoMarteEncontrado = true; break;
            case TITA: save.segredoTitaEncontrado = true; break;
            case CALISTO: save.segredoCalistoEncontrado = true; break;
            default: break;
        }
        save.salvar();
        RouteSystem.tryActivate(save);
    }

    /**
     * Checa um clique do mouse (ja convertido para coordenadas de mundo) contra o
     * retangulo da bancada escondida. Retorna true se o segredo acabou de ser
     * revelado agora (o chamador pode usar isso para mostrar uma mensagem sutil).
     */
    public static boolean checarClique(GameSaveData save, String mundo, Rectangle segredoRect,
                                        float mouseWorldX, float mouseWorldY) {
        if (save == null || segredoRect == null) return false;
        if (jaEncontrado(save, mundo)) return false;
        if (!segredoRect.contains(mouseWorldX, mouseWorldY)) return false;
        marcarEncontrado(save, mundo);
        return true;
    }
}
