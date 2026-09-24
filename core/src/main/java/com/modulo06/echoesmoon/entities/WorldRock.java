package com.modulo06.echoesmoon.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

/** Rocha estatica que bloqueia o movimento do astronauta. */
public class WorldRock {
    public final Rectangle rect;

    public WorldRock(float x, float y, float size) {
        rect = new Rectangle(x, y, size, size);
    }

    public static void spawnMany(Array<WorldRock> rocks, int amount,
                                 float worldWidth, float worldHeight,
                                 Rectangle protectedPlayer,
                                 Array<Rectangle> forbiddenAreas,
                                 float minDistanceFromPlayer) {
        spawnMany(rocks, amount, worldWidth, worldHeight, protectedPlayer, forbiddenAreas, minDistanceFromPlayer, 0x5EEDL);
    }

    public static void spawnMany(Array<WorldRock> rocks, int amount,
                                 float worldWidth, float worldHeight,
                                 Rectangle protectedPlayer,
                                 Array<Rectangle> forbiddenAreas,
                                 float minDistanceFromPlayer, long seed) {
        Random random = new Random(seed);
        int attempts = 0;
        while (rocks.size < amount && attempts++ < amount * 100) {
            float size = 32f + random.nextFloat() * 26f;
            float x = 32f + random.nextFloat() * Math.max(1f, worldWidth - size - 64f);
            float y = 32f + random.nextFloat() * Math.max(1f, worldHeight - size - 64f);
            Rectangle candidate = new Rectangle(x, y, size, size);

            float dx = candidate.x - protectedPlayer.x;
            float dy = candidate.y - protectedPlayer.y;
            float min = minDistanceFromPlayer + size;
            if (dx * dx + dy * dy < min * min) continue;

            boolean blocked = false;
            if (forbiddenAreas != null) {
                for (Rectangle area : forbiddenAreas) {
                    if (candidate.overlaps(area)) {
                        blocked = true;
                        break;
                    }
                }
            }
            if (blocked) continue;

            boolean nearRock = false;
            for (WorldRock rock : rocks) {
                if (candidate.overlaps(expand(rock.rect, 10f))) {
                    nearRock = true;
                    break;
                }
            }
            if (!nearRock) rocks.add(new WorldRock(x, y, size));
        }
    }

    private static Rectangle expand(Rectangle r, float amount) {
        return new Rectangle(r.x - amount, r.y - amount,
                r.width + amount * 2f, r.height + amount * 2f);
    }
}
