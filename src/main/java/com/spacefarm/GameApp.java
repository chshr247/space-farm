package com.spacefarm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacefarm.input.CameraController;
import com.spacefarm.input.GameInputRouter;
import com.spacefarm.render.GameSceneRenderer;
import com.spacefarm.render.MainMenuOverlay;
import com.spacefarm.save.SaveManager;
import com.spacefarm.session.GameSession;

public class GameApp extends ApplicationAdapter {

    private static final float MIN_ZOOM     = 0.5f;
    private static final float MAX_ZOOM     = 2.5f;
    private static final float DEFAULT_ZOOM = 3.5f;
    private enum AppState { MENU, PLAYING }
    private AppState appState = AppState.MENU;
    // Menu
    private MainMenuOverlay mainMenu;
    private SaveManager     saveManager;

    private OrthographicCamera camera;
    private Viewport           viewport;
    private GameSession        session;
    private CameraController   cameraController;
    private GameSceneRenderer  sceneRenderer;
    private float              autosaveTimer    = 0f;
    private static final float AUTOSAVE_INTERVAL = 60f;

    @Override
    public void create() {
        saveManager = new SaveManager();
        mainMenu    = new MainMenuOverlay(saveManager.hasSaveFile());
        appState    = AppState.MENU;
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, false);
            if (cameraController != null) cameraController.clamp();
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (appState == AppState.MENU) {
            tickMenu();
        } else {
            tickGame();
        }
    }

    @Override
    public void pause() {
        if (session != null) saveManager.save(session);
    }

    @Override
    public void dispose() {
        if (mainMenu  != null) mainMenu.dispose();
        if (sceneRenderer != null) sceneRenderer.dispose();
        if (session   != null) {
            saveManager.save(session);
            session.dispose();
        }
    }

    // Menu tick
    private void tickMenu() {
        MainMenuOverlay.Action action = mainMenu.handleInput();
        mainMenu.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        switch (action) {
            case NEW_EASY:
                startNewGame(DifficultyLevel.EASY);
                break;
            case NEW_NORMAL:
                startNewGame(DifficultyLevel.NORMAL);
                break;
            case NEW_HARD:
                startNewGame(DifficultyLevel.HARD);
                break;
            case CONTINUE:
                continueGame();
                break;
            case QUIT:
                Gdx.app.exit();
                break;
            default:
                break;
        }
    }

    // Game tick

    private void tickGame() {
        float delta = Gdx.graphics.getDeltaTime();
        session.update(delta);

        autosaveTimer += delta;
        if (autosaveTimer >= AUTOSAVE_INTERVAL) {
            saveManager.save(session);
            autosaveTimer = 0f;
        }

        if (!session.isGameOver()) {
            cameraController.update(delta);
        }

        camera.update();
        sceneRenderer.render(camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // Game initialisation
    /**
     * Starts a fresh game with the given difficulty.
     * applyDifficulty() MUST be called before session.create() so all
     * constants (oxygen rate, garden beds, starting money) are set correctly.
     */
    private void startNewGame(DifficultyLevel difficulty) {
        disposeMenu();
        buildCamera();

        session = new GameSession();
        session.applyDifficulty(difficulty);
        session.create(camera);

        buildGameObjects();
        appState = AppState.PLAYING;
    }

    /**
     * Continues from the save file.
     * Difficulty-dependent constants were already baked into the save; since the saved
     * wallet / oxygen values will be restored by SaveManager.load().
     */
    private void continueGame() {
        disposeMenu();
        buildCamera();

        session = new GameSession();
        session.applyDifficulty(DifficultyLevel.NORMAL);
        session.create(camera);
        saveManager.load(session);

        buildGameObjects();
        appState = AppState.PLAYING;
    }

    private void buildCamera() {
        camera      = new OrthographicCamera();
        camera.zoom = DEFAULT_ZOOM;
        viewport    = new FitViewport(1280, 720, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    }

    private void buildGameObjects() {
        cameraController = new CameraController(
                camera, viewport, session.getWorldBounds(), MIN_ZOOM, MAX_ZOOM);
        GameInputRouter inputRouter = new GameInputRouter(cameraController, session);
        sceneRenderer   = new GameSceneRenderer(session);
        Gdx.input.setInputProcessor(inputRouter);
    }

    private void disposeMenu() {
        if (mainMenu != null) {
            mainMenu.dispose();
            mainMenu = null;
        }
    }
}