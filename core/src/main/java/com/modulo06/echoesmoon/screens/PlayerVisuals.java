package com.modulo06.echoesmoon.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** Recurso visual reutilizavel para as arenas dos bosses. */
public final class PlayerVisuals {
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> walk;
    private final Rectangle bounds;
    private float stateTime = 0f;
    private boolean moving = false;
    private Texture idleTexture;
    private Texture walkTexture;

    public PlayerVisuals(Rectangle bounds) {
        this.bounds = bounds;
        idleTexture = load("player_lunar.png");
        walkTexture = load("player_lunar_walk.png");
        idle = makeAnimation(idleTexture, 0.20f);
        walk = makeAnimation(walkTexture, 0.12f);
    }

    private Texture load(String path) {
        return Gdx.files.internal(path).exists() ? new Texture(path) : null;
    }

    private Animation<TextureRegion> makeAnimation(Texture texture, float frameDuration) {
        if (texture == null || texture.getWidth() < 4) return null;
        int frameWidth = texture.getWidth() / 4;
        TextureRegion[][] rows = TextureRegion.split(texture, frameWidth, texture.getHeight());
        if (rows.length == 0 || rows[0].length == 0) return null;
        return new Animation<>(frameDuration, rows[0]);
    }

    public void update(float delta, boolean moving) {
        this.moving = moving;
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        Animation<TextureRegion> animation = moving && walk != null ? walk : idle;
        if (animation == null) return;
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        batch.draw(frame, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void dispose() {
        if (idleTexture != null) idleTexture.dispose();
        if (walkTexture != null) walkTexture.dispose();
    }
}
