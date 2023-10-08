package com.thestbar.ludumdare54.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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

import java.util.*;

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

    // Tab menu
    private boolean isPowerupMenuOpen = false;
    private Stage powerupStage;
    private Table powerupRootTable;
    private TextureAtlas powerupGuiAtlas;
    private TextureRegion[] powerupTextures;
    private Array<ImageButton> powerupGuiButtons;
    private ImageButton selectedButton;
    private Map<ImageButton, Integer> guiButtonTypes;
    private Integer[][] powerupGrid;

    private float printPowerupStateTime = 0;

    // TODO - There is a bug on double jump, when the player goes away from a platform without jumping
    public GameScreen(GameApp game) {
        this.game = game;

        // Load all assets to asset manager
        // Textures (Everything is inside the atlas
        game.assetManager.load("spritesheets/atlas/ld54.atlas", TextureAtlas.class);
        game.assetManager.load("spritesheets/ld54-background.png", Texture.class);
        game.assetManager.load("spritesheets/ld54-black-transparent.png", Texture.class);

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
        map = new TmxMapLoader().load("maps/level0/Level_0_v9.tmx");
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

        // Powerup menu UI
        powerupGuiAtlas = new TextureAtlas("skins/powerups-gui/ld54-powerups-gui.atlas");
        powerupTextures = new TextureRegion[8];
        powerupTextures[0] = powerupGuiAtlas.findRegion("ld54-powerup-menu");
        powerupTextures[1] = powerupGuiAtlas.findRegion("ld54-powerup-menu-no-grid");
        for (int i = 1; i < 4; ++i) {
            powerupTextures[i + 1] = powerupGuiAtlas.findRegion("ld54-powerup-" + i);
            powerupTextures[i + 4] = powerupGuiAtlas.findRegion("ld54-powerup-inv" + i);
        }

        powerupStage = new Stage(new ScreenViewport());
        powerupRootTable = new Table();
        powerupRootTable.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        powerupRootTable.setFillParent(true);
//        powerupRootTable.debug();
        powerupRootTable.top();
        powerupStage.addActor(powerupRootTable);
        powerupGuiButtons = new Array<>();
        selectedButton = null;
        guiButtonTypes = new HashMap<>();
        powerupGrid = new Integer[3][3];

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
        inputUpdate(delta);
        cameraUpdate(delta);
        if (!isPowerupMenuOpen) {
            world.step(1/60f, 6, 2);
        } else {
            world.clearForces();
        }
        game.batch.setProjectionMatrix(camera.combined);
        if (!(player.playerState == Player.PlayerState.DIE)) {
            player.render(game.batch, isPowerupMenuOpen);
        } else {
            // This is done to display death animation
            playerDiedDeltaTime += delta;
            if (playerDiedDeltaTime < 1.2f) {
                player.render(game.batch, false);
            }
        }
        if (!isPowerupMenuOpen) {
            for (Player.ActiveEffect effect : player.activeEffects) {
                if (effect.cycle(Gdx.graphics.getDeltaTime())) {
                    for (int i = 0; i < 3; ++i) {
                        powerupGrid[effect.type][i] = null;
                    }
                }
            }
        }
        for (Enemy enemy : Enemy.enemiesArray) {
            float distanceFromPlayer = player.body.getPosition().dst(enemy.body.getPosition());
            Vector2 dir = player.body.getPosition().cpy().sub(enemy.body.getPosition()).nor();
            boolean flip = dir.x > 0;
            if (distanceFromPlayer <= enemy.range) {
                enemy.attack();
            }
            enemy.render(game.batch, flip, isPowerupMenuOpen);
        }
        for (Powerup powerup : Powerup.powerupsArray) {
            powerup.render(game.batch, isPowerupMenuOpen);
        }
        for (Lava lava : Lava.lavaArray) {
            lava.render(game.batch, isPowerupMenuOpen);
        }
        for (Fireball fireball : Fireball.activeFireballs) {
            fireball.render(game.batch, isPowerupMenuOpen);
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

        if (isPowerupMenuOpen) {
//            printPowerupStateTime += Gdx.graphics.getDeltaTime();
//            if (printPowerupStateTime > 5f) {
//                printPowerupStateTime = 0;
//                for (int i = 0; i < 3; ++i) {
//                    for (int j = 0; j < 3; ++j) {
//                        if (powerupGrid[i][j] == null) {
//                            System.out.print("- ");
//                        } else {
//                            System.out.print(powerupGrid[i][j] + " ");
//                        }
//                    }
//                    System.out.println();
//                }
//            }
            // Dim the screen
            game.batch.begin();
            game.batch.draw(game.assetManager.get("spritesheets/ld54-black-transparent.png", Texture.class),
                    0, 0, camera.viewportWidth * Constants.PPM, camera.viewportHeight * Constants.PPM);
            game.batch.draw(powerupTextures[0], camera.position.x + 27, camera.position.y - 33,
                    powerupTextures[0].getRegionWidth() / 3f, powerupTextures[0].getRegionHeight() / 3f);
            game.batch.draw(powerupTextures[1], camera.position.x - 80, camera.position.y - 33,
                    powerupTextures[0].getRegionWidth() / 3f, powerupTextures[0].getRegionHeight() / 3f);
            game.batch.end();
            // Display the power up panel
            initPowerupGui();

            Gdx.input.setInputProcessor(powerupStage);

            // If a power up is selected then display it on the combine section
            if (selectedButton != null) {
                int selectedPowerupTypeId = guiButtonTypes.get(selectedButton);
                // Find selected button's position in the grid
                int secRow, secCol = -1, firstRow = -1, firstCol = -1;
                boolean foundFirst = false;
                outerLoop:
                for (secRow = 0; secRow < 3; ++secRow) {
                    for (secCol = 0; secCol < 3; ++secCol) {
                        if (powerupGrid[secRow][secCol] != null && powerupGrid[secRow][secCol] == selectedPowerupTypeId + 3) {
                            if (foundFirst) {
                                break outerLoop;
                            } else {
                                firstRow = secRow;
                                firstCol = secCol;
                                foundFirst = true;
                            }
                        }
                    }
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                    // Try to move left
                    int newFirstCol = firstCol - 1;
                    int newSecCol = secCol - 1;
                    if (newFirstCol >= 0 && newSecCol >= 0 &&
                            (powerupGrid[firstRow][newFirstCol] == null || powerupGrid[firstRow][newFirstCol] == selectedPowerupTypeId + 3) &&
                            (powerupGrid[secRow][newSecCol] == null || powerupGrid[secRow][newSecCol] == selectedPowerupTypeId + 3)) {
                        // Move it
                        powerupGrid[firstRow][firstCol] = null;
                        powerupGrid[secRow][secCol] = null;
                        powerupGrid[firstRow][newFirstCol] = selectedPowerupTypeId + 3;
                        powerupGrid[secRow][newSecCol] = selectedPowerupTypeId + 3;
                    }
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                    // Try to move right
                    int newFirstCol = firstCol + 1;
                    int newSecCol = secCol + 1;
                    if (newFirstCol < 3 && newSecCol < 3 &&
                            (powerupGrid[firstRow][newFirstCol] == null || powerupGrid[firstRow][newFirstCol] == selectedPowerupTypeId + 3) &&
                            (powerupGrid[secRow][newSecCol] == null || powerupGrid[secRow][newSecCol] == selectedPowerupTypeId + 3)) {
                        // Move it
                        powerupGrid[firstRow][firstCol] = null;
                        powerupGrid[secRow][secCol] = null;
                        powerupGrid[firstRow][newFirstCol] = selectedPowerupTypeId + 3;
                        powerupGrid[secRow][newSecCol] = selectedPowerupTypeId + 3;
                    }
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    // Try to move up
                    int newFirstRow = firstRow - 1;
                    int newSecRow = secRow - 1;
                    if (newFirstRow >= 0 && newSecRow >= 0 &&
                            (powerupGrid[newFirstRow][firstCol] == null || powerupGrid[newFirstRow][firstCol] == selectedPowerupTypeId + 3) &&
                            (powerupGrid[newSecRow][secCol] == null || powerupGrid[newSecRow][secCol] == selectedPowerupTypeId + 3)) {
                        powerupGrid[firstRow][firstCol] = null;
                        powerupGrid[secRow][secCol] = null;
                        powerupGrid[newFirstRow][firstCol] = selectedPowerupTypeId + 3;
                        powerupGrid[newSecRow][secCol] = selectedPowerupTypeId + 3;
                    }
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    // Try to move down
                    int newFirstRow = firstRow + 1;
                    int newSecRow = secRow + 1;
                    if (newFirstRow < 3 && newSecRow < 3 &&
                            (powerupGrid[newFirstRow][firstCol] == null || powerupGrid[newFirstRow][firstCol] == selectedPowerupTypeId + 3) &&
                            (powerupGrid[newSecRow][secCol] == null || powerupGrid[newSecRow][secCol] == selectedPowerupTypeId + 3)) {
                        powerupGrid[firstRow][firstCol] = null;
                        powerupGrid[secRow][secCol] = null;
                        powerupGrid[newFirstRow][firstCol] = selectedPowerupTypeId + 3;
                        powerupGrid[newSecRow][secCol] = selectedPowerupTypeId + 3;
                    }
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    // Add the power up to the grid
                    if (firstRow != -1 && powerupGrid[firstRow][firstCol] != null && powerupGrid[secRow][secCol] != null) {
                        powerupGrid[firstRow][firstCol] -= 3;
                        powerupGrid[secRow][secCol] -= 3;
                        selectedButton.setChecked(false);
                        powerupGuiButtons.removeValue(selectedButton, true);
                        guiButtonTypes.remove(selectedButton);
                        // Delete and re-draw all the power ups in the inventory
                        selectedButton.remove();
                        selectedButton = null;
                        player.collectedPowerupTypes.removeValue(selectedPowerupTypeId, true);
                        // Check if any active effect should be initialized
                        if (powerupGrid[0][0] != null && powerupGrid[0][1] != null && powerupGrid[0][2] != null) {
                            player.activatePowerUp(0, 15);
                        }
                        if (powerupGrid[1][0] != null && powerupGrid[1][1] != null && powerupGrid[1][2] != null) {
                            player.activatePowerUp(1, 15);
                        }
                        if (powerupGrid[2][0] != null && powerupGrid[2][1] != null && powerupGrid[2][2] != null) {
                            player.activatePowerUp(2, 15);
                        }
                    }
                }
            }

            powerupStage.getViewport().apply();
            powerupStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            powerupStage.draw();

            // Draw tiles in combine
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    if (powerupGrid[i][j] != null) {
                        // We have to draw it
                        int typeId = powerupGrid[i][j];
                        if (typeId > 2) {
                            typeId -= 3;
                        }
                        game.batch.begin();
                        game.batch.draw(powerupTextures[2 + typeId], camera.position.x + 27 + j * 53 / 3f, camera.position.y - 33 - i * 53 / 3f,
                                powerupTextures[0].getRegionWidth() / 3f, powerupTextures[0].getRegionHeight() / 3f);
                        game.batch.end();
                    }
                }
            }
            game.stage.getViewport().apply();
        } else {
            // Clear powerup gui table
            powerupRootTable.clear();
            powerupRootTable.clear();
            Gdx.input.setInputProcessor(game.stage);
            selectedButton = null;
            guiButtonTypes.clear();
        }
    }

    private void initPowerupGui() {
        if (powerupRootTable.getRows() > 0) {
            return;
        }
        Label title = new Label("Power Ups Menu", this.game.skin);
        title.setStyle(LabelStyleUtil.getLabelStyle(this.game, "subtitle", Color.WHITE));
        title.setFontScale(0.8f);
        powerupRootTable.add(title).colspan(6).expandX().height(100).padTop(100).row();

        Label title1 = new Label("Inventory", this.game.skin);
        title1.setStyle(LabelStyleUtil.getLabelStyle(this.game, "subtitle", Color.WHITE));
        title1.setFontScale(0.6f);
        powerupRootTable.add(title1).colspan(3).expandX().padBottom(20);

        Label title2 = new Label("Combine", this.game.skin);
        title2.setStyle(LabelStyleUtil.getLabelStyle(this.game, "subtitle", Color.WHITE));
        title2.setFontScale(0.6f);
        powerupRootTable.add(title2).colspan(3).expandX().padBottom(20).row();

        Table innerTable = new Table();
        Table powerupFontsTable = new Table();

        Label font1 = new Label("x2 Damage", this.game.skin);
        font1.setStyle(LabelStyleUtil.getLabelStyle(this.game, "font", Color.WHITE));
        font1.setFontScale(0.8f);
        powerupFontsTable.add(font1).left().padTop(30).row();

        Label font2 = new Label("x2 HP", this.game.skin);
        font2.setStyle(LabelStyleUtil.getLabelStyle(this.game, "font", Color.WHITE));
        font2.setFontScale(0.8f);
        powerupFontsTable.add(font2).left().padTop(60).row();

        Label font3 = new Label("x2 Jump", this.game.skin);
        font3.setStyle(LabelStyleUtil.getLabelStyle(this.game, "font", Color.WHITE));
        font3.setFontScale(0.8f);
        powerupFontsTable.add(font3).left().padTop(60).row();

        powerupRootTable.add(innerTable).top().left().padLeft(140).colspan(3).expandX();
        powerupRootTable.add(powerupFontsTable).left().padLeft(410).colspan(3).expandX();

        int i = 0;
        for (final int powerupTypeId : player.collectedPowerupTypes) {
            final ImageButton button = getImageButton(powerupTypeId);
            button.setProgrammaticChangeEvents(false);
            button.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeListener.ChangeEvent) {
                        for (int i = 0; i < 3; ++i) {
                            for (int j = 0; j < 3; ++j) {
                                if (powerupGrid[i][j] != null && powerupGrid[i][j] > 2) {
                                    powerupGrid[i][j] = null;
                                }
                            }
                        }
                        if (selectedButton != null) {
                            selectedButton.setChecked(false);
                        }
                        selectedButton = button;
                        // Find available spot
                        switch (powerupTypeId) {
                            case 0: // Yellow
                                System.out.println("Adding Yellow");
                                findPositionForNewYellow(powerupTypeId);
                                break;
                            case 1: // Red
                                System.out.println("Adding Red");
                                findPositionForNewRed(powerupTypeId);
                                break;
                            case 2: // Green
                                System.out.println("Adding Green");
                                findPositionForNewGreen(powerupTypeId);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                    return false;
                }
            });
            powerupGuiButtons.add(button);
            guiButtonTypes.put(button, powerupTypeId);
            innerTable.add(button);
            ++i;
            if (i == 5) {
                innerTable.row();
                i = 0;
            }
        }
    }

    private void findPositionForNewGreen(int powerupTypeId) {
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                if (powerupGrid[i][j] == null && powerupGrid[i + 1][j + 1] == null) {
                    // Found
                    powerupGrid[i][j] = powerupTypeId + 3;
                    powerupGrid[i + 1][j + 1] = powerupTypeId + 3;
                    return;
                }
            }
        }
    }

    private void findPositionForNewRed(int powerupTypeId) {
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 3; ++j) {
                if (powerupGrid[i][j] == null && powerupGrid[i + 1][j] == null) {
                    // Found
                    powerupGrid[i][j] = powerupTypeId + 3;
                    powerupGrid[i + 1][j] = powerupTypeId + 3;
                    return;
                }
            }
        }
    }

    private void findPositionForNewYellow(int powerupTypeId) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 2; ++j) {
                if (powerupGrid[i][j] == null && powerupGrid[i][j + 1] == null) {
                    // Found
                    powerupGrid[i][j] = powerupTypeId + 3;
                    powerupGrid[i][j + 1] = powerupTypeId + 3;
                    return;
                }
            }
        }
    }

    private ImageButton getImageButton(int powerupTypeId) {
        String buttonStyle;
        switch(powerupTypeId) {
            case 0:
                buttonStyle = "powerup-button-yellow";
                break;
            case 1:
                buttonStyle = "powerup-button-red";
                break;
            case 2:
                buttonStyle = "powerup-button-green";
                break;
            default:
                buttonStyle = "";
                break;
        }
        return new ImageButton(game.skin, buttonStyle);
    }

    private void inputUpdate(float delta) {
        if (player.playerState == Player.PlayerState.DIE ||
            player.playerState == Player.PlayerState.WIN) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            isPowerupMenuOpen = !isPowerupMenuOpen;
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    if (powerupGrid[i][j] != null && powerupGrid[i][j] > 2) {
                        powerupGrid[i][j] = null;
                    }
                }
            }
        }
        // In case the powerup menu is open do not let the
        // user move the character
        if (isPowerupMenuOpen) {
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
//        System.out.println(camera.position);
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
        powerupStage.dispose();
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
