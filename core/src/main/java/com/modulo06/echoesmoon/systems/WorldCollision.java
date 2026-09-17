package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.WorldRock;

/** Colisao simples e estavel com as pedras do mapa. */
public final class WorldCollision {
    private WorldCollision() {}

    public static void movePlayer(Rectangle player, float dx, float dy,
                                  Array<WorldRock> rocks,
                                  float worldWidth, float worldHeight) {
        // Eixo X separado para o player nao atravessar a rocha nos cantos.
        player.x += dx;
        player.x = MathUtils.clamp(player.x, 0f, worldWidth - player.width);
        if (overlapsRock(player, rocks)) {
            player.x -= dx;
            player.x = MathUtils.clamp(player.x, 0f, worldWidth - player.width);
        }

        player.y += dy;
        player.y = MathUtils.clamp(player.y, 0f, worldHeight - player.height);
        if (overlapsRock(player, rocks)) {
            player.y -= dy;
            player.y = MathUtils.clamp(player.y, 0f, worldHeight - player.height);
        }
    }

    public static boolean overlapsRock(Rectangle actor, Array<WorldRock> rocks) {
        for (WorldRock rock : rocks) {
            if (actor.overlaps(rock.rect)) return true;
        }
        return false;
    }
}
