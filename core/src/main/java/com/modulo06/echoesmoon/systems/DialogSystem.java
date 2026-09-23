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
    private boolean aguardandoEscolha = false;

    // Inicia o dialogo recebendo as frases e a imagem (portrait) do NPC.
    // O portrait pode ser null.
    public void start(String[] lines, Texture portrait) {
        this.lines = lines;
        this.currentLine = 0;
        this.currentPortrait = portrait;
        this.open = lines != null && lines.length > 0;
        this.aguardandoEscolha = false;
    }

    public void startChoice(String prompt, Texture portrait) {
        start(new String[]{prompt}, portrait);
        aguardandoEscolha = true;
    }

    public boolean aguardandoEscolha() { return aguardandoEscolha; }
    public void escolher() { aguardandoEscolha = false; }

    // Avanca para a proxima frase ou fecha se acabar.
    public void next() {
        if (!open) return;

        currentLine++;
        if (currentLine >= lines.length) {
            open = false;
        }
    }

    public boolean isOpen() {
        return open;
    }

    // Desenha a caixa de dialogo.
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (!open || lines == null || lines.length == 0) return;

        // Fundo preto.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 1f);
        shapeRenderer.rect(150, 30, 980, 180);
        shapeRenderer.end();

        // Borda branca.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(150, 30, 1130, 30, 6);
        shapeRenderer.rectLine(150, 210, 1130, 210, 6);
        shapeRenderer.rectLine(150, 30, 150, 210, 6);
        shapeRenderer.rectLine(1130, 30, 1130, 210, 6);
        shapeRenderer.end();

        batch.begin();

        // Portrait e opcional.
        // Nunca chame batch.draw() com Texture null.
        if (currentPortrait != null) {
            batch.draw(currentPortrait, 170, 50, 140, 140);
        }

        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;

        font.getData().setScale(1.2f);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "* " + lines[currentLine], 340, 170);
        if (aguardandoEscolha) font.draw(batch, "[1] Rota diplomatica     [2] Rota agressiva", 340, 115);

        // Restaura a escala original do font.
        font.getData().setScale(oldScaleX, oldScaleY);

        batch.end();
    }
}
