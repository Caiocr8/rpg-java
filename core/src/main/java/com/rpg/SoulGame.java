package com.rpg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SoulGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        // Inicializa o desenhista principal do jogo
        batch = new SpriteBatch();

        // Inicia o jogo chamando a tela de Menu Arcane
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        // O render aqui apenas delega o desenho para a tela que está ativa (Menu, Play, etc)
        super.render();
    }

    @Override
    public void dispose() {
        // Limpa a memória quando o jogo fecha
        if (batch != null) {
            batch.dispose();
        }
        super.dispose();
    }
}
