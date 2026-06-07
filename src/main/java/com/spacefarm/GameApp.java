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
import com.spacefarm.save.SaveManager;
import com.spacefarm.session.GameSession;

public class GameApp extends ApplicationAdapter {
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.5f;
    private static final float DEFAULT_ZOOM = 3.5f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private GameSession session;
    private CameraController cameraController;
    private GameSceneRenderer sceneRenderer;
    private SaveManager saveManager;
    private float autosaveTimer = 0f;
    private static final float AUTOSAVE_INTERVAL = 60f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.zoom = DEFAULT_ZOOM;
        viewport = new FitViewport(1280, 720, camera);

        session = new GameSession();
        session.create(camera);

        saveManager = new SaveManager();
        if (saveManager.hasSaveFile()) {
            saveManager.load(session);
        }

        cameraController = new CameraController(camera, viewport, session.getWorldBounds(), MIN_ZOOM, MAX_ZOOM);
        GameInputRouter inputRouter = new GameInputRouter(cameraController, session);
        sceneRenderer = new GameSceneRenderer(session);

        Gdx.input.setInputProcessor(inputRouter);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        cameraController.clamp();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        session.update(deltaTime);

        autosaveTimer += deltaTime;
        if (autosaveTimer >= AUTOSAVE_INTERVAL) {
            if (saveManager != null && session != null) {
                saveManager.save(session);
            }
            autosaveTimer = 0f;
        }

        if (!session.isGameOver()) {
            cameraController.update(deltaTime);
        }

        camera.update();
        sceneRenderer.render(camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void pause() {
        if (saveManager != null && session != null) {
            saveManager.save(session);
        }
    }

    @Override
    public void dispose() {
        if (saveManager != null && session != null) {
            saveManager.save(session);
        }
        if (sceneRenderer != null) sceneRenderer.dispose();
        if (session != null) session.dispose();
    }
}