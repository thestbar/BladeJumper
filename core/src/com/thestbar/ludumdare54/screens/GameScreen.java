package com.thestbar.ludumdare54.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.thestbar.ludumdare54.GameApp;
import com.thestbar.ludumdare54.utils.Constants;
import com.thestbar.ludumdare54.utils.TiledObjectUtil;

import static com.thestbar.ludumdare54.utils.Constants.PPM;

public class GameScreen implements Screen {
    private GameApp game;
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera camera;
    private Body player;
    private Texture playerTex;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private TiledMap map;

    public GameScreen(GameApp game) {
        this.game = game;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width / Constants.SCALE, height / Constants.SCALE);

        world = new World(Constants.GRAVITATIONAL_CONSTANT, true);
        debugRenderer = new Box2DDebugRenderer();

        player = createBox(200, 200, 10, 14, BodyDef.BodyType.DynamicBody);

        playerTex = new Texture(Gdx.files.internal("spritesheets/ld54-player.png"));
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

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Constants.DEBUG_BACKGROUND_COLOR);

        world.step(1/60f, 6, 2);
        inputUpdate(delta);
        cameraUpdate(delta);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(playerTex, player.getPosition().x * PPM - playerTex.getWidth() / 2f,
                player.getPosition().y * PPM - playerTex.getHeight() / 2f);
        game.batch.end();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        debugRenderer.render(world, camera.combined.scl(PPM));
    }

    private void inputUpdate(float delta) {
        int horizontalForce = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontalForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontalForce += 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player.applyForceToCenter(0, 500, true);
        }
        player.setLinearVelocity(horizontalForce * 7, player.getLinearVelocity().y);
    }

    private Body createBox(int x, int y, int width, int height, BodyDef.BodyType bodyType) {
        Body body;
        BodyDef def = new BodyDef();
        def.type = bodyType;
        def.position.set(x / PPM, y / PPM);
        def.fixedRotation = true;
        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / PPM, height / 2f / PPM);

        body.createFixture(shape, 1.0f);
        shape.dispose();

        return body;
    }

    private void cameraUpdate(float delta) {
        Vector3 position = camera.position;
        position.x = player.getPosition().x * PPM;
        position.y = player.getPosition().y * PPM;
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
        playerTex.dispose();
        tiledMapRenderer.dispose();
        map.dispose();
    }
}
