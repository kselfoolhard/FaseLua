package com.modulo06.echoesmoon.systems;

public final class UpgradeSystem {
    public static final int MAX_LEVEL = 3;
    public static final int KILLS_PER_WEAPON = 5;
    public static final int KILLS_PER_ARMOR = 8;

    private UpgradeSystem() {}

    public static boolean registerKill(GameSaveData save) {
        if (save == null) return false;
        save.inimigosDerrotados++;
        // Abates geram apenas creditos. Upgrades agora sao comprados exclusivamente na loja.
        save.creditos += (int)(Math.random() < 0.25 ? 10 : 5);
        save.salvar();
        return false;
    }

    public static boolean comprarArma(GameSaveData save) {
        if (save == null || save.inventario == null || save.inventario.nivelArma >= MAX_LEVEL) return false;
        int proximo = save.inventario.nivelArma + 1;
        int preco = precoArma(proximo);
        if (save.creditos < preco) return false;
        save.creditos -= preco;
        save.inventario.nivelArma = proximo;
        save.ultimoUpgrade = "UPGRADE DE ARMA NV." + proximo;
        save.salvar();
        return true;
    }

    public static boolean comprarArmadura(GameSaveData save) {
        if (save == null || save.inventario == null || save.inventario.nivelArmadura >= MAX_LEVEL) return false;
        int proximo = save.inventario.nivelArmadura + 1;
        int preco = precoArmadura(proximo);
        if (save.creditos < preco) return false;
        save.creditos -= preco;
        save.inventario.nivelArmadura = proximo;
        save.ultimoUpgrade = "UPGRADE DE ARMADURA NV." + proximo;
        save.salvar();
        return true;
    }

    public static int precoArma(int nivel) { return 35 + Math.max(0, nivel - 1) * 25; }
    public static int precoArmadura(int nivel) { return 45 + Math.max(0, nivel - 1) * 30; }

    public static int danoArma(GameSaveData save) {
        int level = save == null || save.inventario == null ? 0 : save.inventario.nivelArma;
        return 10 + 5 * Math.min(MAX_LEVEL, level);
    }

    public static float multiplicadorDanoRecebido(GameSaveData save) {
        int level = save == null || save.inventario == null ? 0 : save.inventario.nivelArmadura;
        float multiplicadorBase = Math.max(0.45f, 1f - 0.15f * Math.min(MAX_LEVEL, level));

        if (save != null && "FACIL".equals(save.dificuldade)) multiplicadorBase *= 0.75f;
        if (save != null && "DIFICIL".equals(save.dificuldade)) multiplicadorBase *= 1.25f;

        // ALTERAÇÃO: A escolha de rotas agora afeta diretamente a jogabilidade.
        if (RouteSystem.isAggressive(save)) {
            multiplicadorBase *= 1.5f; // Rota agressiva causa mais dano recebido.
        }
        return multiplicadorBase;
    }

    public static float danoRecebido(GameSaveData save, float bruto) {
        return bruto * multiplicadorDanoRecebido(save);
    }

    public static void aplicarDano(GameSaveData save, float bruto) {
        if (save == null) return;
        float dano = danoRecebido(save, bruto);
        float resto = dano - save.escudo;
        save.escudo = Math.max(0f, save.escudo - dano);
        if (resto > 0f) save.vida -= resto;
        SoundManager.playSound("hit_player");
    }

    public static String progresso(GameSaveData save) {
        // ... (Mantido igual)
        return "ABATES: " + save.inimigosDerrotados;
    }
}
