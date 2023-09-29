package com.thestbar.ludumdare54.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;
import com.thestbar.ludumdare54.GameApp;
import com.thestbar.ludumdare54.LabelStyleUtil;

public class MainMenu implements Screen {
    private final GameApp game;
    private Table rootTable;
    private TextButton startGameButton;
    private Label titleLabel;

    // Title animation variables
    private float titleLabelSize = 2f;
    private float animationSpeed = 0.5f;
    private boolean isAnimationStage2 = false;
    private boolean isSizeDec = true;

    float counter = 0;
    public MainMenu(GameApp game) {
        this.game = game;

        rootTable = new Table();
        rootTable.setFillParent(true);
//        rootTable.debug();
        this.game.stage.addActor(rootTable);

        titleLabel = new Label("Ludum Dare 54", this.game.skin);
        titleLabel.setStyle(LabelStyleUtil.getLabelStyle(this.game, "title", Color.ORANGE));
        titleLabel.setFontScale(titleLabelSize);
        rootTable.add(titleLabel).row();

        Label nextLabel = new Label("Let's have fun on this weekend", this.game.skin);
        nextLabel.setStyle(LabelStyleUtil.getLabelStyle(this.game, "subtitle", Color.WHITE));
        rootTable.add(nextLabel).padTop(50).row();

        startGameButton = new TextButton("Start", this.game.skin);
        rootTable.add(startGameButton).padTop(80).row();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        game.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        game.stage.draw();
        counter += delta * 10;

        // Animate title!
        // Initially starting from 2f go down to 1f
        if (!isAnimationStage2) {
            titleLabelSize -= delta * animationSpeed;
            if (titleLabelSize < 1f) {
                titleLabelSize = 1f;
                isAnimationStage2 = true;
                isSizeDec = false;
                animationSpeed = 0.1f;
            }
        }
        // Then just go back and forth between 1f and 1.2f
        // Reduce speed for phase 2 -> reduced at then of phase 1
        else {
            float factor = (isSizeDec) ? -1f : 1f;
            titleLabelSize += delta * animationSpeed * factor;
            if (isSizeDec && titleLabelSize < 1f) {
                isSizeDec = false;
            } else if (!isSizeDec && titleLabelSize > 1.1f) {
                isSizeDec = true;
            }
        }

        titleLabel.setFontScale(titleLabelSize);

        if (startGameButton.isPressed()) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
