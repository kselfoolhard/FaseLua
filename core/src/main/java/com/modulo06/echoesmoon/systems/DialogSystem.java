package com.modulo06.echoesmoon.systems;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DialogSystem {
    private String[] lines;
    private int currentLine = 0;
    private boolean open = false;
    private Texture currentPortrait;

    // Inicia o diálogo recebendo as frases e a imagem (portrait) do NPC
    public void start(String[] lines, Texture portrait) {
        this.lines = lines;
        this.currentLine = 0;
        this.currentPortrait = portrait;
        this.open = true;
    }

    // Avança para a próxima frase ou fecha se acabar
    public void next() {
        if (open) {
            currentLine++;
            if (currentLine >= lines.length) {
                open = false;
            }
        }
    }

    public boolean isOpen() {
        return open;
    }

    // Desenha a caixa no estilo Undertale
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (!open) return;

        // Fundo Preto
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 1f);
        shapeRenderer.rect(150, 30, 980, 180);
        shapeRenderer.end();

        // Borda Branca
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(150, 30, 1130, 30, 6);
        shapeRenderer.rectLine(150, 210, 1130, 210, 6);
        shapeRenderer.rectLine(150, 30, 150, 210, 6);
        shapeRenderer.rectLine(1130, 30, 1130, 210, 6);
        shapeRenderer.end();

        // Texto e Portrait
        batch.begin();
        if (currentPortrait != null) {
            // Desenha a foto do Oficial ou do Rádio à esquerda
            batch.draw(currentPortrait, 170, 50, 140, 140);
        }
        font.getData().setScale(1.2f);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "* " + lines[currentLine], 340, 170);
        font.getData().setScale(1f);
        batch.draw(currentPortrait, 0, 0, 0, 0); // Reset invisível pro batch não bugar
        batch.end();
    }
}
