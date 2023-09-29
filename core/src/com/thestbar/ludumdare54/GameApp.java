package com.thestbar.ludumdare54;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.thestbar.ludumdare54.screens.MainMenu;

public class GameApp extends Game {
	public SpriteBatch batch;
	public ShapeRenderer renderer;
	public Stage stage;
	public Skin skin;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skins/pixthulu/pixthulhu-ui.json"));

		renderer = new ShapeRenderer();
		renderer.setAutoShapeType(true);
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		this.setScreen(new MainMenu(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		renderer.dispose();
		stage.dispose();
		skin.dispose();
	}
}
