package com.modulo06.echoesmoon.entities;


import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class InimigoTita {
    public static final int STATE_CHASE = 0;
    public static final int STATE_WINDUP = 1;
    public static final int STATE_ATTACK = 2;

    public Rectangle rect;
    public float hp;
    public float maxHp;
    public boolean ativo;
    public int state;
    public float stateTimer;
    public Vector2 targetPos;
    public boolean rockThrown;

    public InimigoTita(float x, float y) {
        this.rect = new Rectangle(x, y, 96, 96);
        this.maxHp = 4000f;
        this.hp = maxHp;
        this.ativo = true;
        this.state = STATE_CHASE;
        this.stateTimer = 0f;
        this.targetPos = new Vector2();
        this.rockThrown = false;
    }

    public void update(float delta, Vector2 playerPos) {
        if (!ativo) return;

        stateTimer += delta;

        switch (state) {
            case STATE_CHASE:
                // Segue o player rápido (estilo Tank L4D)
                Vector2 dir = new Vector2(playerPos.x - rect.x, playerPos.y - rect.y).nor();
                rect.x += dir.x * 180f * delta;
                rect.y += dir.y * 180f * delta;

                // A cada 4 segundos ele para para preparar o arremesso
                if (stateTimer >= 4.0f) {
                    state = STATE_WINDUP;
                    stateTimer = 0f;
                    targetPos.set(playerPos.x + 16, playerPos.y + 16); // Trava o alvo no player
                    rockThrown = false;
                }
                break;

            case STATE_WINDUP:
                // Parado carregando o golpe por 1.5s (Tempo pro jogador desviar)
                if (stateTimer >= 0.5f) {
                    state = STATE_ATTACK;
                    stateTimer = 0f;
                }
                break;

            case STATE_ATTACK:
                rockThrown = true;
                // Volta a perseguir o player
                state = STATE_CHASE;
                stateTimer = 0f;
                break;
        }
    }

    public void takeDamage(float damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            ativo = false;
        }
    }
}
