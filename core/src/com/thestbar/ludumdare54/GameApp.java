package com.thestbar.ludumdare54;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.thestbar.ludumdare54.managers.SoundManager;
import com.thestbar.ludumdare54.screens.GameScreen;
import com.thestbar.ludumdare54.screens.MainMenu;

public class GameApp extends Game {
	public SpriteBatch batch;
	public ShapeRenderer renderer;
	public Stage stage;
	public Skin skin;
	public AssetManager assetManager;
	public final static boolean IS_DEV_ENV = false;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("skins/pixthulu/pixthulhu-ui.json"));
		renderer = new ShapeRenderer();
		renderer.setAutoShapeType(true);
		assetManager = new AssetManager();
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
		assetManager.dispose();
	}
}
