package com.thestbar.ludumdare54;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.thestbar.ludumdare54.screens.GameScreen;
import com.thestbar.ludumdare54.screens.MainMenu;

public class GameApp extends Game {
	public SpriteBatch batch;
	public ShapeRenderer renderer;
	public Stage stage;
	public Skin skin;
	public final static boolean IS_DEV_ENV = true;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skins/pixthulu/pixthulhu-ui.json"));

		renderer = new ShapeRenderer();
		renderer.setAutoShapeType(true);

		this.setScreen((IS_DEV_ENV) ? new GameScreen(this) : new MainMenu(this));
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
