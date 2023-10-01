package com.thestbar.ludumdare54.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.thestbar.ludumdare54.GameApp;
import com.thestbar.ludumdare54.Player;
import com.thestbar.ludumdare54.listeners.ListenerClass;
import com.thestbar.ludumdare54.utils.Constants;
import com.thestbar.ludumdare54.utils.TiledObjectUtil;

import static com.thestbar.ludumdare54.utils.Constants.PPM;

public class GameScreen implements Screen {
    private GameApp game;
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private TiledMap map;
    private Player player;
    private ListenerClass listener;

    // TODO - Fix bug with 2 missing colliders
    public GameScreen(GameApp game) {
        this.game = game;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width / Constants.SCALE, height / Constants.SCALE);

        world = new World(Constants.GRAVITATIONAL_CONSTANT, true);
        debugRenderer = new Box2DDebugRenderer();

        player = new Player(world);
        // The fileName path is based on project's working directory.
        // It is very important to keep the structure of the LDtk program
        // output. The tmx file uses the other resources to construct itself.
        // Remember also to set the "image source" attribute of the tmx file
        // to "image source="../../../spritesheets/ld54-spritesheet.png".
        // This is because we want the tmx file to point to the sprite sheet.
        map = new TmxMapLoader().load("assets/maps/level0/tiled/Level_0.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(map);

        TiledObjectUtil.parseTiledObjectLayer(world, map.getLayers().get("colliders").getObjects());
    }

    @Override
    public void show() {
        listener = new ListenerClass();
        world.setContactListener(listener);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Constants.DEBUG_BACKGROUND_COLOR);

        world.step(1/60f, 6, 2);
        inputUpdate(delta);
        cameraUpdate(delta);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        player.render(game.batch);
        game.batch.end();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        debugRenderer.render(world, camera.combined.scl(PPM));
    }

    private void inputUpdate(float delta) {
        if (player == null) {
            return;
        }
        int horizontalForce = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontalForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontalForce += 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && listener.isPlayerOnGround()) {
            player.jump(800);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.W) && !listener.isPlayerOnGround() && listener.isAvailableDoubleJump()) {
            player.jump(1000);
            listener.useDoubleJump();
        }
        player.move(horizontalForce);
    }

    private void cameraUpdate(float delta) {
        Vector3 position = camera.position;
        // b = a + (b - a) * lerp
        // b = target
        // a = current position
        final float lerp = 0.1f;
        position.x = position.x + (player.body.getPosition().x * PPM - position.x) * lerp;
        position.y = position.y + (player.body.getPosition().y * PPM - position.y) * lerp;
        position.z = 10f;
        camera.position.set(position);

        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / Constants.SCALE, height / Constants.SCALE);
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
        world.dispose();
        debugRenderer.dispose();
        player.dispose();
        tiledMapRenderer.dispose();
        map.dispose();
    }
}
