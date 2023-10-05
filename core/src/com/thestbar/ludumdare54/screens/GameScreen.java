package com.thestbar.ludumdare54.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.thestbar.ludumdare54.*;
import com.thestbar.ludumdare54.gameobjects.*;
import com.thestbar.ludumdare54.listeners.ListenerClass;
import com.thestbar.ludumdare54.managers.SoundManager;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;
import com.thestbar.ludumdare54.utils.LabelStyleUtil;
import com.thestbar.ludumdare54.utils.TiledObjectUtil;

public class GameScreen implements Screen {
    private GameApp game;
    private Box2DDebugRenderer debugRenderer;
    private World world;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private TiledMap map;
    public static Player player;
    private ListenerClass listener;
    public static Array<Body> bodiesToBeDeleted;
    private MapObject levelEndPos;
    private float playerDiedDeltaTime;
    private SoundManager soundManager;

    // Play again UI
    private Table rootTable;
    private TextButton startGameButton;
    private Label titleLabel;
    private Label nextLabel;

    // Title animation variables
    private float titleLabelSize = 2f;
    private Stage uiStage;
    private ProgressBar uiPlayerHealthBar;

    // TODO - There is a bug on double jump, when the player goes away from a platform without jumping
    public GameScreen(GameApp game) {
        this.game = game;

        // Load all assets to asset manager
        // Textures
        game.assetManager.load("spritesheets/ld54-background.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-healrthbar-Sheet.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-enemies-Sheet.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-enemy3-bullet-Sheet.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-black-transparent.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-powerups-Sheet.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-lava-Sheet.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-mainmenu-background.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-player-Sheet.png", Texture.class);
        // Music and sound effects
        soundManager = new SoundManager(game.assetManager);

        game.assetManager.finishLoading();
    }

    @Override
    public void show() {
        this.game.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(this.game.stage);

        bodiesToBeDeleted = new Array<>();

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width, height);

        world = new World(Constants.GRAVITATIONAL_CONSTANT, false);
        debugRenderer = new Box2DDebugRenderer();

        // The fileName path is based on project's working directory.
        // It is very important to keep the structure of the LDtk program
        // output. The tmx file uses the other resources to construct itself.
        // Remember also to set the "image source" attribute of the tmx file
        // to "image source="../../../spritesheets/ld54-spritesheet.png".
        // This is because we want the tmx file to point to the sprite sheet.
        map = new TmxMapLoader().load("maps/level0/tiled/Level_0_v8.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(map);

        TiledObjectUtil.parseTiledObjectLayer(world, map.getLayers().get("colliders").getObjects());

        // Create player
        MapObjects entities = map.getLayers().get("Entities").getObjects();
        MapObject playerStartPos;

        if (entities.get(0).getName().equals("PlayerStart")) {
            playerStartPos = entities.get(0);
            levelEndPos = entities.get(1);
        } else {
            playerStartPos = entities.get(1);
            levelEndPos = entities.get(0);
        }
        Rectangle rectangle = ((RectangleMapObject) playerStartPos).getRectangle();
        int x = (int) rectangle.x;
        int y = (int) rectangle.y;
        player = new Player(game, world, x, y, soundManager);
        camera.position.x = 143;
        camera.position.y = 807;
        camera.position.z = 10f;
        camera.update();

        rectangle = ((RectangleMapObject) levelEndPos).getRectangle();
        x = (int) rectangle.x;
        y = (int) rectangle.y;
        Body levelEndPoint = Box2DUtils
                .createBox(world, x, y, 16, 10, BodyDef.BodyType.StaticBody);
        levelEndPoint.getFixtureList().get(0).setDensity(1);
        levelEndPoint.getFixtureList().get(0).setFriction(0);
        levelEndPoint.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_LEVEL_END;
        levelEndPoint.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_PLAYER;
        levelEndPoint.getFixtureList().get(0).setSensor(true);
        levelEndPoint.getFixtureList().get(0).setUserData("level_end");

        // Create enemies
        Enemy.createEnemies(game, world, map.getLayers().get("Enemies").getObjects(), soundManager);

        // Create powerups
        Powerup.createPowerups(game, world, map.getLayers().get("Powerups").getObjects());

        // Create lava
        Lava.createLavas(game, world, map.getLayers().get("Lava").getObjects());

        // Play again UI initialization - Is being used and as win screen
        rootTable = new Table();
        rootTable.setFillParent(true);
        game.stage.addActor(rootTable);

        titleLabel = new Label("You Died!", this.game.skin);
        titleLabel.setStyle(LabelStyleUtil.getLabelStyle(this.game, "title", Color.ORANGE));
        titleLabel.setFontScale(titleLabelSize);
        rootTable.add(titleLabel).row();
        titleLabel.setFontScale(0.8f);

        nextLabel = new Label("\"Don't smash the keyboard, yet!\"", this.game.skin);
        nextLabel.setStyle(LabelStyleUtil.getLabelStyle(this.game, "subtitle", Color.WHITE));
        rootTable.add(nextLabel).padTop(50).row();
        nextLabel.setFontScale(0.8f);

        startGameButton = new TextButton("Restart", this.game.skin);
        rootTable.add(startGameButton).padTop(80).row();

        // Build Game UI
        uiStage = new Stage(new ScreenViewport());
        Table uiRootTable = new Table();
        uiRootTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiRootTable.top().left();
        uiRootTable.setFillParent(true);
        uiStage.addActor(uiRootTable);

        uiPlayerHealthBar = new ProgressBar(0, player.maxHealthPoints, 1, false, game.skin);
        uiPlayerHealthBar.setStyle(new ProgressBar
                .ProgressBarStyle(game.skin.get("health", ProgressBar.ProgressBarStyle.class)));
        uiPlayerHealthBar.setValue(player.maxHealthPoints);
        uiRootTable.add(uiPlayerHealthBar).width(500).padTop(20).padLeft(20);

//        uiRootTable.debug();

        listener = new ListenerClass();
        world.setContactListener(listener);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        if (!game.assetManager.update(17)) {
            float progress = game.assetManager.getProgress();
            System.out.println("Loading: " + progress);
            return;
        }
        if (!soundManager.isBackgroundMusicOn()) {
            soundManager.playBackgroundMusic();
        }
        game.batch.begin();
        game.batch.draw(game.assetManager.get("spritesheets/ld54-background.png", Texture.class),
                0, 0, camera.viewportWidth * Constants.SCALE,
                camera.viewportHeight * Constants.SCALE * 2);
        game.batch.end();
        world.step(1/60f, 6, 2);
        inputUpdate(delta);
        cameraUpdate(delta);
        game.batch.setProjectionMatrix(camera.combined);
        if (!(player.playerState == Player.PlayerState.DIE)) {
            player.render(game.batch);
        } else {
            // This is done to display death animation
            playerDiedDeltaTime += delta;
            if (playerDiedDeltaTime < 1.2f) {
                player.render(game.batch);
            }
        }
        for (Enemy enemy : Enemy.enemiesArray) {
            float distanceFromPlayer = player.body.getPosition().dst(enemy.body.getPosition());
            Vector2 dir = player.body.getPosition().cpy().sub(enemy.body.getPosition()).nor();
            boolean flip = dir.x > 0;
            if (distanceFromPlayer <= enemy.range) {
                enemy.attack();
            }
            enemy.render(game.batch, flip);
        }
        for (Powerup powerup : Powerup.powerupsArray) {
            powerup.render(game.batch);
        }
        for (Lava lava : Lava.lavaArray) {
            lava.render(game.batch);
        }
        for (Fireball fireball : Fireball.activeFireballs) {
            fireball.render(game.batch);
        }
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
//        debugRenderer.render(world, camera.combined.scl(Constants.PPM));

        // Dispose unused bodies
        for (Body body : bodiesToBeDeleted) {
            world.destroyBody(body);
        }
        bodiesToBeDeleted.clear();

        // Update player health bar
        uiPlayerHealthBar.setValue(player.healthPoints);
        // Render UI

        uiStage.getViewport().apply();
        uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        uiStage.draw();
        game.stage.getViewport().apply();

        // In case player dies render try again screen
        // In case player wins render win screen
        if (player.playerState == Player.PlayerState.DIE ||
            player.playerState == Player.PlayerState.WIN) {
            if (player.playerState == Player.PlayerState.WIN) {
                titleLabel.setText("You Won!");
                nextLabel.setText("Thanks for playing!");
            }
            game.batch.begin();
            game.batch.draw(game.assetManager.get("spritesheets/ld54-black-transparent.png", Texture.class),
                    0, 0, camera.viewportWidth * Constants.PPM, camera.viewportHeight * Constants.PPM);
            game.batch.end();
            game.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            game.stage.draw();
            if (startGameButton.isPressed()) {
                // Remember to dispose all the static memory
                soundManager.playSound("button");
                disposeStaticMemory();
                game.setScreen(new GameScreen(game));
            }
        }
    }

    private void inputUpdate(float delta) {
        if (player.playerState == Player.PlayerState.DIE ||
            player.playerState == Player.PlayerState.WIN) {
            return;
        }
        int horizontalForce = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontalForce -= 1;
            player.playerState = Player.PlayerState.MOVE_LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontalForce += 1;
            player.playerState = Player.PlayerState.MOVE_RIGHT;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && listener.isPlayerOnGround()) {
            player.jump(800);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.W) && !listener.isPlayerOnGround() && listener.isAvailableDoubleJump()) {
            player.jump(1000);
            listener.useDoubleJump();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.attack();
            // Check for hits on enemies
            int enemyIdBeingHit = listener.getEnemyIdBeingHit();
            if (enemyIdBeingHit != -1 && player.isAttacking()) {
                String enemyId = "enemy" + enemyIdBeingHit;
                Enemy enemy = Enemy.enemiesMap.get(enemyId);
                enemy.hit(player.playerDamage);
            }
        }
        player.move(horizontalForce);
    }

    private void cameraUpdate(float delta) {
        System.out.println(camera.position);
        Vector3 position = camera.position;
        // b = a + (b - a) * lerp
        // b = target
        // a = current position
        final float lerp = 0.1f;
        position.x = position.x + (player.body.getPosition().x * Constants.PPM - position.x) * lerp;
        position.y = position.y + (player.body.getPosition().y * Constants.PPM - position.y) * lerp;
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
        game.assetManager.clear();
    }

    public void disposeStaticMemory() {
        Enemy.enemiesArray.clear();
        Enemy.enemiesMap.clear();
        Enemy.enemies = 0;
        Fireball.activeFireballs.clear();
        Fireball.fireballMap.clear();
        Fireball.fireballCounter = 0;
        Lava.lavaArray.clear();
        Powerup.powerupMap.clear();
        Powerup.powerupsArray.clear();
        Powerup.powerups = 0;
        GameScreen.bodiesToBeDeleted.clear();
        GameScreen.player = null;
    }
}
