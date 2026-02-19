package com.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

    final SoulGame game;
    Texture logo;
    BitmapFont menuFont;
    ShapeRenderer shapeRenderer;
    GlyphLayout layout;

    // Sistema Profissional de Câmera (mantém proporção em qualquer tela)
    OrthographicCamera camera;
    Viewport viewport;
    final float V_WIDTH = 1280;
    final float V_HEIGHT = 720;

    int commandNum = 0;
    String[] options = {"NOVO JOGO", "CONTINUAR", "CONFIGURACOES", "SAIR"};
    float animationCounter = 0;

    // Paleta de Cores "Arcane Soul"
    Color colorBg = Color.valueOf("1a0022"); // Fundo super escuro
    Color colorElement = Color.valueOf("4b0082"); // Roxo escuro
    Color colorBorder = Color.valueOf("8a2be2"); // Borda roxa
    Color colorGlow = Color.valueOf("00e5ff"); // Ciano Neon

    // --- SISTEMA DE PARTÍCULAS (Magic Embers) ---
    class MagicDust {
        float x, y, speed, size, alpha;
        Color color;

        public MagicDust() {
            reset();
            this.y = MathUtils.random(0, V_HEIGHT); // Espalha pela tela ao iniciar
        }

        public void reset() {
            this.x = MathUtils.random(0, V_WIDTH);
            this.y = -10;
            this.speed = MathUtils.random(15f, 50f);
            this.size = MathUtils.random(2f, 5f);
            this.alpha = MathUtils.random(0.2f, 0.7f);
            this.color = MathUtils.randomBoolean(0.8f) ? colorGlow : Color.valueOf("d8b2ff");
        }

        public void update(float delta) {
            y += speed * delta;
            x += MathUtils.sin(y * 0.05f) * 0.5f;
            if (y > V_HEIGHT + 10) {
                reset();
            }
        }
    }

    Array<MagicDust> dustParticles;
    // ---------------------------------------------

    public MainMenuScreen(final SoulGame game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        layout = new GlyphLayout();

        camera = new OrthographicCamera();
        viewport = new FitViewport(V_WIDTH, V_HEIGHT, camera);
        camera.position.set(V_WIDTH / 2f, V_HEIGHT / 2f, 0);

        logo = new Texture(Gdx.files.internal("rpg-soul.png"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("caveat.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.color = Color.WHITE;
        menuFont = generator.generateFont(parameter);
        generator.dispose();

        dustParticles = new Array<>();
        for (int i = 0; i < 100; i++) {
            dustParticles.add(new MagicDust());
        }
    }

    @Override
    public void render(float delta) {
        animationCounter += delta;

        // 1. OBRIGATÓRIO: Atualiza a câmera antes de ler o mouse
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);

        // --- SISTEMA DE ENTRADA (MOUSE E TECLADO) ---
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos); // Converte o clique para 1280x720
        boolean mouseMexeu = Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0;

        int btnWidth = 350;
        int btnHeight = 60;
        int btnX = (int)(V_WIDTH - btnWidth) / 2;
        int baseY = (int)(V_HEIGHT / 2) - 20;

        // Verifica hover (mouse em cima) e cliques
        for (int i = 0; i < options.length; i++) {
            int btnY = baseY - (i * (btnHeight + 20));

            // Hitbox do botão
            if (mousePos.x >= btnX && mousePos.x <= btnX + btnWidth &&
                mousePos.y >= btnY && mousePos.y <= btnY + btnHeight) {

                if (mouseMexeu) {
                    commandNum = i; // Atualiza a seleção visual
                }

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    executarAcao(commandNum);
                }
            }
        }

        // Teclado (Navegação)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            commandNum--;
            if (commandNum < 0) commandNum = options.length - 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            commandNum++;
            if (commandNum > options.length - 1) commandNum = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            executarAcao(commandNum);
        }
        // --------------------------------------------

        // LIMPA A TELA
        ScreenUtils.clear(colorBg);

        // 2. DESENHA PARTÍCULAS (Com transparência Alpha)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (MagicDust dust : dustParticles) {
            dust.update(delta);
            shapeRenderer.setColor(dust.color.r, dust.color.g, dust.color.b, dust.alpha);
            shapeRenderer.circle(dust.x, dust.y, dust.size);
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 3. DESENHA BOTÕES
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < options.length; i++) {
            int btnY = baseY - (i * (btnHeight + 20));

            if (commandNum == i) {
                // Brilho do botão selecionado
                shapeRenderer.setColor(colorGlow);
                shapeRenderer.rect(btnX - 4, btnY - 4, btnWidth + 8, btnHeight + 8);
                shapeRenderer.setColor(colorElement);
                shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight);
            } else {
                // Botão normal
                shapeRenderer.setColor(colorBorder);
                shapeRenderer.rect(btnX - 2, btnY - 2, btnWidth + 4, btnHeight + 4);
                shapeRenderer.setColor(Color.valueOf("1a001a"));
                shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight);
            }
        }
        shapeRenderer.end();

        // 4. DESENHA LOGO E TEXTOS
        game.batch.begin();
        float logoWidth = 450;
        float logoHeight = (logo.getHeight() * logoWidth) / logo.getWidth();
        float logoX = (V_WIDTH - logoWidth) / 2;
        float logoY = V_HEIGHT - logoHeight - 30 + (float) Math.sin(animationCounter * 3f) * 10f;
        game.batch.draw(logo, logoX, logoY, logoWidth, logoHeight);

        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            int btnY = baseY - (i * (btnHeight + 20));

            layout.setText(menuFont, opt);
            float textX = (V_WIDTH - layout.width) / 2;
            float textY = btnY + (btnHeight + layout.height) / 2;

            menuFont.setColor(commandNum == i ? colorGlow : Color.WHITE);
            menuFont.draw(game.batch, opt, textX, textY);
        }
        game.batch.end();
    }

    // Gerencia o que acontece quando clica ou aperta Enter
    private void executarAcao(int acao) {
        if (acao == 0) {
            System.out.println("Iniciando Novo Jogo...");
            // game.setScreen(new PlayScreen(game)); // PRÓXIMO PASSO AQUI!
        } else if (acao == 1) {
            System.out.println("Continuando Jogo Salvo...");
        } else if (acao == 2) {
            System.out.println("Abrindo Configuracoes...");
        } else if (acao == 3) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        logo.dispose();
        menuFont.dispose();
        shapeRenderer.dispose();
    }
}
