package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.Vector2;

import com.modulo06.echoesmoon.entities.InimigoTita;

public class CombatSystem {
    public float alcance = 80f;
    public float cooldown = 0f, cooldownMax = 0.40f;
    public int municao = 8;
    public int dano = 25;

    // Mecânicas de Slash (Espada de Energia) e Reload
    public float reloadCooldown = 0f, reloadMax = 1.5f;
    public boolean isSlashing = false;
    public float slashTimer = 0f;

    public void update(float delta) {
        if (cooldown > 0f) cooldown -= delta;
        if (cooldown < 0f) cooldown = 0f;

        if (reloadCooldown > 0f) {
            reloadCooldown -= delta;
            if (reloadCooldown <= 0f) municao = 8; // Recarga concluída
        }

        if (isSlashing) {
            slashTimer -= delta;
            if (slashTimer <= 0f) isSlashing = false;
        }
    }

    public boolean tentarTiro(Vector2 origem, InimigoTita alvo, boolean temArma) {
        if (!temArma || municao <= 0 || cooldown > 0f || reloadCooldown > 0f) return false;
        if (alvo == null || !alvo.vivo()) return false;

        // Ativa o rastro visual do slash ao atacar
        isSlashing = true;
        slashTimer = 0.2f;

        if (origem.dst(alvo.pos) > alcance) {
            municao--;
            cooldown = cooldownMax;
            return false;
        }

        alvo.hp -= dano;
        municao--;
        cooldown = cooldownMax;
        return true;
    }

    public void forcarReload() {
        if (reloadCooldown <= 0f && municao < 8) {
            reloadCooldown = reloadMax;
        }
    }
}
