package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.modulo06.echoesmoon.entities.WorldRock;

public final class WorldCollision {
    private WorldCollision() {}

    public static void movePlayer(Rectangle player, float dx, float dy, Array<WorldRock> rocks, float worldWidth, float worldHeight) {
        player.x += dx;
        player.x = MathUtils.clamp(player.x, 0f, worldWidth - player.width);
        if (overlapsRock(player, rocks)) {
            player.x -= dx;
        }

        player.y += dy;
        player.y = MathUtils.clamp(player.y, 0f, worldHeight - player.height);
        if (overlapsRock(player, rocks)) {
            player.y -= dy;
        }
    }

    public static boolean overlapsRock(Rectangle actor, Array<WorldRock> rocks) {
        for (WorldRock rock : rocks) {
            if (actor.overlaps(rock.rect)) return true;
        }
        return false;
    }

    /** ALTERAÇÃO: Encontra um local válido onde o tubo de O2 não fique encravado nas rochas */
    public static Vector2 encontrarPontoSeguroDrop(float startX, float startY, Array<WorldRock> rocks) {
        Rectangle test = new Rectangle(startX, startY, 32, 32); // Tamanho aproximado do O2
        if (!overlapsRock(test, rocks)) return new Vector2(startX, startY);

        // Testa posições adjacentes
        float[] offsets = {-40, 40, -80, 80};
        for (float ox : offsets) {
            for (float oy : offsets) {
                test.setPosition(startX + ox, startY + oy);
                if (!overlapsRock(test, rocks)) {
                    return new Vector2(test.x, test.y);
                }
            }
        }
        return new Vector2(startX, startY); // Fallback
    }
}
